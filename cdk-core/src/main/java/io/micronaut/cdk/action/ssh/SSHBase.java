/*
 * Copyright 2017-2026 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.cdk.action.ssh;

import io.micronaut.cdk.ExecutionContext;
import io.micronaut.cdk.action.Executable;
import io.micronaut.cdk.action.Secret;
import io.micronaut.cdk.component.IpAddressReference;
import io.micronaut.cdk.util.Assert;
import io.micronaut.cdk.util.CDKUtils;
import com.jcraft.jsch.ConfigRepository.Config;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.OpenSSHConfig;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.Session;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Base class for SCP and SSH Exec.
 * <p>
 * Based on code from org.apache.tools.ant.taskdefs.optional.ssh.
 *
 * @param <A> the type
 */
public abstract class SSHBase<A extends SSHBase<A>> extends Executable<A> {

    private static final int SSH_PORT = 22;
    private static final int DEFAULT_PROXY_PORT = 80;
    private static final int DEFAULT_RETRY_MAX_COUNT = 10;
    private static final float DEFAULT_RETRY_MAX_DELAY = 30_000;
    private static final int DEFAULT_RETRY_DELAY = 1000;
    private static final float DEFAULT_RETRY_MULTIPLIER = 2;

    private final Logger jschLogger = LoggerFactory.getLogger(getClass().getName() + ".jsch");

    private final Integer connectTimeout;
    private final String knownHostsPath;
    private final Secret<String> passphrase;
    private final Secret<String> password;
    private final int port;
    private String privateKeyPath;
    private final String proxyHost;
    private final Integer proxyPort;
    private String remoteHost;
    private Map<String, String> resolvedIpAddresses;
    private final int retryDelay;
    private final int retryMaxCount;
    private final float retryMaxDelay;
    private final float retryMultiplier;
    private final int serverAliveCountMax;
    private final int serverAliveIntervalSeconds;
    private String sshConfigPath;
    private final boolean trustAllCertificates;
    private String username;

    /**
     * Constructor.
     *
     * @param cdkId                      CDK ID
     * @param stacks                     stacks
     * @param ipAddressReferences        IP address references
     * @param connectTimeout             connect timeout seconds
     * @param knownHostsPath             location of known_hosts, e.g. ~/.ssh/known_hosts
     * @param passphrase                 the private key passphrase
     * @param password                   the password
     * @param port                       SSH port
     * @param privateKeyPath             location of the file containing the private key, e.g. ~/.ssh/id_rsa
     * @param proxyHost                  HTTP proxy host
     * @param proxyPort                  HTTP proxy port
     * @param remoteHost                 remote host URL or IP address
     * @param retryDelay                 connection retry delay
     * @param retryMaxCount              connection retry max attempts
     * @param retryMaxDelay              connection retry max delay between attempts
     * @param retryMultiplier            connection retry delay multipler
     * @param serverAliveCountMax        the number of keep-alive messages which
     *                                   may be sent without receiving any messages
     *                                   back from the server; only used if
     *                                   serverAliveIntervalSeconds is not 0
     * @param serverAliveIntervalSeconds the interval to send a keep-alive message
     *                                   if no data has been received
     * @param sshConfigPath              location of SSH config file, e.g. ~/.ssh/config
     * @param trustAllCertificates       whether to trust the identity of unknown hosts
     * @param username                   the username
     * @throws SSHException if there's a problem
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    protected SSHBase(@NonNull String cdkId,
                      @NonNull Collection<String> stacks,
                      @NonNull Collection<IpAddressReference> ipAddressReferences,
                      @Nullable Integer connectTimeout,
                      @Nullable String knownHostsPath,
                      @NonNull Secret<String> passphrase,
                      @NonNull Secret<String> password,
                      int port,
                      @Nullable String privateKeyPath,
                      @Nullable String proxyHost,
                      @Nullable Integer proxyPort,
                      @NonNull String remoteHost,
                      int retryDelay,
                      int retryMaxCount,
                      float retryMaxDelay,
                      float retryMultiplier,
                      int serverAliveCountMax,
                      int serverAliveIntervalSeconds,
                      @Nullable String sshConfigPath,
                      boolean trustAllCertificates,
                      @Nullable String username) throws SSHException {
        super(cdkId, stacks, ipAddressReferences);

        Assert.state(serverAliveCountMax > 0, "serverAliveCountMax cannot be negative or zero");
        Assert.state(serverAliveIntervalSeconds >= 0, "serverAliveIntervalSeconds cannot be negative");

        if (!trustAllCertificates && knownHostsPath != null) {
            knownHostsPath = CDKUtils.resolveTilde(knownHostsPath);
            assertExists(knownHostsPath);
        }

        this.connectTimeout = connectTimeout;
        this.knownHostsPath = knownHostsPath;
        this.passphrase = passphrase;
        this.password = password;
        this.port = port;
        this.privateKeyPath = privateKeyPath;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.remoteHost = Assert.notNull(remoteHost, "remoteHost cannot be null");
        this.retryDelay = retryDelay;
        this.retryMaxCount = retryMaxCount;
        this.retryMaxDelay = retryMaxDelay;
        this.retryMultiplier = retryMultiplier;
        this.serverAliveCountMax = serverAliveCountMax;
        this.serverAliveIntervalSeconds = serverAliveIntervalSeconds;
        this.sshConfigPath = sshConfigPath;
        this.trustAllCertificates = trustAllCertificates;
        this.username = username;
    }

    /**
     * Initialize.
     *
     * @param ec the execution context
     */
    protected void init(@NonNull ExecutionContext ec) {

        resolvedIpAddresses = resolveIpAddresses(ec);

        if (sshConfigPath != null && (username == null || privateKeyPath == null)) {
            sshConfigPath = CDKUtils.resolveTilde(sshConfigPath);
            assertExists(sshConfigPath);

            logger.debug("Loading SSH configuration file {}", sshConfigPath);
            Config config;
            try {
                config = OpenSSHConfig.parseFile(sshConfigPath).getConfig(remoteHost);
            } catch (IOException e) {
                throw new SSHException("Failed to load the SSH configuration file " + sshConfigPath, e);
            }

            if (config.getHostname() != null) {
                remoteHost = config.getHostname();
            }

            if (username == null) {
                username = config.getUser();
            }

            if (privateKeyPath == null) {
                logger.info("Using SSH key file {} for host {}", config.getValue("IdentityFile"), remoteHost);
                privateKeyPath = config.getValue("IdentityFile");
            }
        }

        Assert.state(privateKeyPath != null || password.getValue() != null, "password or privateKeyPath is required");
        if (privateKeyPath != null) {
            privateKeyPath = CDKUtils.resolveTilde(privateKeyPath);
            assertExists(privateKeyPath);
        }
        Assert.notNull(username, "username must be specified or loaded from SSH config");

        if (remoteHost != null) {
            remoteHost = resolveIpAddresses(remoteHost, resolvedIpAddresses);
        }
    }

    /**
     * Resolved IP addresses.
     *
     * @return the addresses
     */
    protected Map<String, String> getResolvedIpAddresses() {
        return Collections.unmodifiableMap(resolvedIpAddresses);
    }

    /**
     * Open a session.
     *
     * @return a new session
     * @throws JSchException if there's a problem
     */
    protected Session openSession() throws JSchException {

        JSch jsch = new JSch();

        JSch.setLogger(new com.jcraft.jsch.Logger() {

            @Override
            public boolean isEnabled(int level) {
                switch (level) {
                    case com.jcraft.jsch.Logger.DEBUG:
                    case com.jcraft.jsch.Logger.INFO:
                        return jschLogger.isDebugEnabled();
                    case com.jcraft.jsch.Logger.WARN:
                        return jschLogger.isInfoEnabled();
                    case com.jcraft.jsch.Logger.ERROR:
                    case com.jcraft.jsch.Logger.FATAL:
                        return jschLogger.isErrorEnabled();
                    default:
                        return false;
                }
            }

            @Override
            public void log(int level, String message) {
                switch (level) {
                    case com.jcraft.jsch.Logger.DEBUG:
                    case com.jcraft.jsch.Logger.INFO:
                        jschLogger.debug(message);
                        break;
                    case com.jcraft.jsch.Logger.WARN:
                        jschLogger.info(message);
                        break;
                    case com.jcraft.jsch.Logger.ERROR:
                    case com.jcraft.jsch.Logger.FATAL:
                        jschLogger.error(message);
                        break;
                    default:
                }
            }
        });

        if (privateKeyPath != null) {
            jsch.addIdentity(privateKeyPath);
        }

        if (!trustAllCertificates && knownHostsPath != null) {
            logger.debug("Using known hosts: {}", knownHostsPath);
            jsch.setKnownHosts(knownHostsPath);
        }

        Session session = jsch.getSession(username, remoteHost, port);
        session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password"); // TODO
        session.setUserInfo(new CdkUserInfo(password.getValue(), passphrase.getValue(), trustAllCertificates));
        if (proxyHost != null && proxyPort != null) {
            session.setProxy(new ProxyHTTP(proxyHost, proxyPort));
        }

        if (serverAliveIntervalSeconds > 0) {
            session.setServerAliveCountMax(serverAliveCountMax);
            session.setServerAliveInterval(serverAliveIntervalSeconds * 1000);
        }

        logger.info("Connecting to {}:{}", remoteHost, port);
        connect(session);

        return session;
    }

    private void connect(Session session) throws JSchException {
        float delay = 0;

        for (int i = 0; i < retryMaxCount; i++) {

            if (delay == 0) {
                delay = retryDelay;
            } else {
                logger.info("Connection refused for retry {} of {}, waiting {}ms before next retry", i, retryMaxCount, delay);
                try {
                    Thread.sleep((int) delay);
                } catch (InterruptedException ignored) {
                }
            }
            delay *= retryMultiplier;
            if (delay > retryMaxDelay) {
                delay = retryMaxDelay;
            }

            try {
                if (connectTimeout == null) {
                    session.connect();
                } else {
                    session.connect(connectTimeout);
                }
                return;
            } catch (JSchException e) {
                if (!(e.getMessage().contains("Connection refused") ||
                        e.getMessage().equals("connection is closed by foreign host") ||
                        e.getCause() instanceof ConnectException)) {
                    throw e;
                }
            }
        }

        throw new SSHException("Exceeded max wait/attempts connecting to " + remoteHost);
    }

    private void assertExists(String path) {
        Assert.exists(new File(path));
    }

    @Override
    public String toString() {
        return super.toString() +
                ", knownHostsPath='" + knownHostsPath + '\'' +
                ", passphrase=" + passphrase +
                ", password=" + password +
                ", port=" + port +
                ", proxyHost=" + quote(proxyHost) +
                ", proxyPort=" + proxyPort +
                ", privateKeyPath='" + privateKeyPath + '\'' +
                ", remoteHost='" + remoteHost + '\'' +
                ", retryDelay=" + retryDelay +
                ", retryMaxCount=" + retryMaxCount +
                ", retryMaxDelay=" + retryMaxDelay +
                ", retryMultiplier=" + retryMultiplier +
                ", serverAliveCountMax=" + serverAliveCountMax +
                ", serverAliveIntervalSeconds=" + serverAliveIntervalSeconds +
                ", trustAllCertificates=" + trustAllCertificates +
                ", username='" + username + '\'';
    }

    /**
     * Builder.
     *
     * @param <A> the action type
     * @param <B> the builder type
     */
    public abstract static class Builder<A extends SSHBase<A>, B extends Builder<A, B>> extends Executable.Builder<A, B> {

        private final String remoteHost;

        private Integer connectTimeout;
        private String knownHostsPath;
        private Secret<String> passphrase = Secret.empty();
        private Secret<String> password = Secret.empty();
        private int port = SSH_PORT;
        private String privateKeyPath;
        private String proxyHost;
        private Integer proxyPort = DEFAULT_PROXY_PORT;
        private int retryMaxCount = DEFAULT_RETRY_MAX_COUNT;
        private float retryMaxDelay = DEFAULT_RETRY_MAX_DELAY;
        private int retryDelay = DEFAULT_RETRY_DELAY;
        private float retryMultiplier = DEFAULT_RETRY_MULTIPLIER;
        private int serverAliveCountMax = 3;
        private int serverAliveIntervalSeconds;
        private String sshConfigPath;
        private boolean trustAllCertificates;
        private String username;

        /**
         * Constructor.
         *
         * @param cdkId      CDK ID
         * @param remoteHost remote host URL or IP address
         */
        protected Builder(@NonNull String cdkId,
                          @NonNull String remoteHost) {
            super(cdkId);
            this.remoteHost = Assert.notNull(remoteHost, "remoteHost cannot be null");
        }

        /**
         * Optional session connect timeout seconds.
         *
         * @param connectTimeout the timeout
         * @return this
         */
        @NonNull
        public B connectTimeout(@Nullable Integer connectTimeout) {
            this.connectTimeout = connectTimeout;
            return self();
        }

        /**
         * Optional session connect timeout seconds.
         *
         * @return the timeout
         */
        @Nullable
        protected Integer getConnectTimeout() {
            return connectTimeout;
        }

        /**
         * Location of known_hosts, e.g. ~/.ssh/known_hosts.
         *
         * @param knownHostsPath the location
         * @return this
         */
        @NonNull
        public B knownHostsPath(@NonNull String knownHostsPath) {
            this.knownHostsPath = knownHostsPath;
            return self();
        }

        /**
         * Location of known_hosts, e.g. ~/.ssh/known_hosts.
         *
         * @return the location
         */
        @Nullable
        protected String getKnownHostsPath() {
            return knownHostsPath;
        }

        /**
         * The passphrase for the private key.
         *
         * @param passphrase the passphrase
         * @return this
         */
        @NonNull
        public B passphrase(@NonNull String passphrase) {
            this.passphrase = new Secret<>(passphrase);
            return self();
        }

        /**
         * The passphrase for the private key.
         *
         * @return the passphrase
         */
        @NonNull
        protected Secret<String> getPassphrase() {
            return passphrase;
        }

        /**
         * Password.
         *
         * @param password password
         * @return this
         */
        @NonNull
        public B password(@NonNull String password) {
            this.password = new Secret<>(password);
            return self();
        }

        /**
         * Password.
         *
         * @return the password
         */
        @NonNull
        protected Secret<String> getPassword() {
            return password;
        }

        /**
         * SSH port.
         *
         * @param port the port
         * @return this
         */
        @NonNull
        public B port(int port) {
            this.port = port;
            return self();
        }

        /**
         * SSH port.
         *
         * @return the port
         */
        protected int getPort() {
            return port;
        }

        /**
         * Location of the file containing the private key, e.g. ~/.ssh/id_rsa.
         *
         * @param privateKeyPath the location
         * @return this
         */
        @NonNull
        public B privateKeyPath(@NonNull String privateKeyPath) {
            this.privateKeyPath = privateKeyPath;
            return self();
        }

        /**
         * Location of the file containing the private key, e.g. ~/.ssh/id_rsa.
         *
         * @return the location
         */
        @Nullable
        protected String getPrivateKeyPath() {
            return privateKeyPath;
        }

        /**
         * Optional HTTP proxy host.
         *
         * @param proxyHost the host
         * @return this
         */
        @NonNull
        public B proxyHost(@Nullable String proxyHost) {
            this.proxyHost = proxyHost;
            return self();
        }

        /**
         * Optional HTTP proxy host.
         *
         * @return the host
         */
        @Nullable
        protected String getProxyHost() {
            return proxyHost;
        }

        /**
         * Optional HTTP proxy port.
         *
         * @param proxyPort the port
         * @return this
         */
        @NonNull
        public B proxyPort(@Nullable Integer proxyPort) {
            this.proxyPort = proxyPort;
            return self();
        }

        /**
         * Optional HTTP proxy port.
         *
         * @return the host
         */
        @Nullable
        protected Integer getProxyPort() {
            return proxyPort;
        }

        /**
         * Remote host.
         *
         * @return remote host
         */
        @NonNull
        protected String getRemoteHost() {
            return remoteHost;
        }

        /**
         * Connection retry delay.
         *
         * @param retryDelay the delay
         * @return this
         */
        @NonNull
        public B retryDelay(@NonNull int retryDelay) {
            this.retryDelay = retryDelay;
            return self();
        }

        /**
         * Connection retry delay.
         *
         * @return the delay
         */
        protected int getRetryDelay() {
            return retryDelay;
        }

        /**
         * Connection retry max attempts.
         *
         * @param retryMaxCount the max
         * @return this
         */
        @NonNull
        public B retryMaxCount(@NonNull int retryMaxCount) {
            this.retryMaxCount = retryMaxCount;
            return self();
        }

        /**
         * Connection retry max attempts.
         *
         * @return the max
         */
        protected int getRetryMaxCount() {
            return retryMaxCount;
        }

        /**
         * Connection retry max delay between attempts.
         *
         * @param retryMaxDelay the max delay
         * @return this
         */
        @NonNull
        public B retryMaxDelay(@NonNull float retryMaxDelay) {
            this.retryMaxDelay = retryMaxDelay;
            return self();
        }

        /**
         * Connection retry max delay between attempts.
         *
         * @return the max delay
         */
        protected float getRetryMaxDelay() {
            return retryMaxDelay;
        }

        /**
         * Connection retry delay multipler.
         *
         * @param retryMultiplier the multiplier
         * @return this
         */
        @NonNull
        public B retryMultiplier(@NonNull float retryMultiplier) {
            this.retryMultiplier = retryMultiplier;
            return self();
        }

        /**
         * Connection retry delay multipler.
         *
         * @return the multiplier
         */
        protected float getRetryMultiplier() {
            return retryMultiplier;
        }

        /**
         * The number of keep-alive messages which may be sent without receiving
         * any messages back from the server; only used if
         * serverAliveIntervalSeconds is not 0.
         *
         * @param serverAliveCountMax max count
         * @return this
         */
        @NonNull
        public B serverAliveCountMax(int serverAliveCountMax) {
            this.serverAliveCountMax = serverAliveCountMax;
            return self();
        }

        /**
         * The number of keep-alive messages which may be sent without receiving
         * any messages back from the server; only used if
         * serverAliveIntervalSeconds is not 0.
         *
         * @return serverAliveCountMax max count
         */
        protected int getServerAliveCountMax() {
            return serverAliveCountMax;
        }

        /**
         * The interval to send a keep-alive message if no data has been received.
         *
         * @param serverAliveIntervalSeconds the interval
         * @return this
         */
        @NonNull
        public B serverAliveIntervalSeconds(int serverAliveIntervalSeconds) {
            this.serverAliveIntervalSeconds = serverAliveIntervalSeconds;
            return self();
        }

        /**
         * The interval to send a keep-alive message if no data has been received.
         *
         * @return the interval
         */
        protected int getServerAliveIntervalSeconds() {
            return serverAliveIntervalSeconds;
        }

        /**
         * The location of SSH config file, e.g. ~/.ssh/config.
         *
         * @param sshConfigPath the location
         * @return this
         */
        @NonNull
        public B sshConfigPath(@NonNull String sshConfigPath) {
            this.sshConfigPath = sshConfigPath;
            return self();
        }

        /**
         * The location of SSH config file, e.g. ~/.ssh/config.
         *
         * @return sshConfigPath the location
         */
        @Nullable
        protected String getSshConfigPath() {
            return sshConfigPath;
        }

        /**
         * Whether to trust the identity of unknown hosts.
         *
         * @param trustAllCertificates true to trust all
         * @return this
         */
        @NonNull
        public B trustAllCertificates(boolean trustAllCertificates) {
            this.trustAllCertificates = trustAllCertificates;
            return self();
        }

        /**
         * Whether to trust the identity of unknown hosts.
         *
         * @return true to trust all
         */
        protected boolean isTrustAllCertificates() {
            return trustAllCertificates;
        }

        /**
         * Username.
         *
         * @param username username
         * @return this
         */
        @NonNull
        public B username(@NonNull String username) {
            this.username = username;
            return self();
        }

        /**
         * Username.
         *
         * @return the username
         */
        @Nullable
        protected String getUsername() {
            return username;
        }
    }
}
