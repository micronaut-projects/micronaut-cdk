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
import io.micronaut.cdk.action.Secret;
import io.micronaut.cdk.component.IpAddressReference;
import io.micronaut.cdk.util.Assert;
import io.micronaut.cdk.util.MultiOutputStream;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Executes SSH commands.
 * <p>
 * Based on code from org.apache.tools.ant.taskdefs.optional.ssh.
 *
 * @param <A> the type
 */
public class SSHExec<A extends SSHExec<A>> extends SSHBase<A> {

    private static final int RETRY_INTERVAL = 500;
    private static final String TIMEOUT_MESSAGE = "Timeout period exceeded, connection dropped.";

    private final Map<String, Predicate<ExecutionContext>> commands;
    private final int timeoutMillis;

    /**
     * @param cdkId                      CDK ID
     * @param stacks                     stacks
     * @param ipAddressReferences        IP address references
     * @param commands                   commands to run
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
     * @param timeoutMillis              the number of milliseconds before the connection
     *                                   will be dropped; defaults to 0, i.e. wait forever
     * @param trustAllCertificates       whether to trust the identity of unknown hosts
     * @param username                   the username
     * @throws SSHException if there's a problem
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    SSHExec(@NonNull String cdkId,
            @NonNull Collection<String> stacks,
            @NonNull Collection<IpAddressReference> ipAddressReferences,
            @NonNull Map<String, Predicate<ExecutionContext>> commands,
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
            int timeoutMillis,
            boolean trustAllCertificates,
            @Nullable String username) throws SSHException {
        super(cdkId, stacks, ipAddressReferences, connectTimeout, knownHostsPath, passphrase, password,
                port, privateKeyPath, proxyHost, proxyPort, remoteHost, retryDelay, retryMaxCount,
                retryMaxDelay, retryMultiplier, serverAliveCountMax, serverAliveIntervalSeconds,
                sshConfigPath, trustAllCertificates, username);
        Assert.notNull(commands, "commands cannot be null");
        Assert.state(!commands.isEmpty(), "at least one command is required");
        this.commands = commands;
        this.timeoutMillis = timeoutMillis;
    }

    @Override
    public List<Result> execute(@NonNull ExecutionContext ec) {

        init(Assert.notNull(ec, "ExecutionContext cannot be null"));

        List<Result> results = new ArrayList<>(commands.size());

        Session session = null;
        try {
            session = openSession();
            for (Map.Entry<String, Predicate<ExecutionContext>> entry : commands.entrySet()) {
                if (entry.getValue() == null || entry.getValue().test(ec)) {
                    Result result = checkForNewVariableAndExecute(ec, entry.getKey(), session);
                    results.add(result);
                    if (result.getException() != null) {
                        break;
                    }
                }
            }
        } catch (JSchException e) {
            throw new SSHException(e);
        } finally {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }

        return results;
    }

    private Result checkForNewVariableAndExecute(@NonNull ExecutionContext ec,
                                                 @NonNull String command,
                                                 @NonNull Session session) {
        // TODO make it better
        Result result = null;
        if (command.contains("$")) {
            command = resolveVariables(ec.getVariables(getCdkId()), command);
            if (command.startsWith("export ")) {
                String variable = command.substring(7);
                int pos = variable.indexOf("=$(");
                if (pos > 0) {
                    variable = variable.substring(0, pos);
                    command = command.substring(pos + 10); // strip command without variable and evaluation string '=$()'
                    if (command.endsWith(")")) {
                        command = command.substring(0, command.length() - 1);
                    }
                    result = executeCommand(command, session);
                    String value = result.getOutput();
                    if (!value.isEmpty()) {
                        ec.registerVariable(getCdkId(), variable, value);
                    }
                }
            } else {
                result = executeCommand(command, session);
            }
        } else {
            result = executeCommand(command, session);
        }
        return result;
    }

    private Result executeCommand(@NonNull String command,
                                  @NonNull Session session) {

        command = resolveIpAddresses(command, getResolvedIpAddresses());

        logger.info("cmd : {}", command);

        boolean info = logger.isInfoEnabled();
        var outputStream = new ByteArrayOutputStream();
        var errorStream = new ByteArrayOutputStream();
        var outputStreamMulti = new MultiOutputStream(info, outputStream);
        var errorStreamMulti = new MultiOutputStream(info, errorStream);
        if (info) {
            outputStreamMulti.addOutputStream(new LoggerOutputStream(true));
            errorStreamMulti.addOutputStream(new LoggerOutputStream(false));
        }

        int exitStatus = 0;
        Exception exception = null;
        try {
            session.setTimeout(timeoutMillis);

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setOutputStream(outputStreamMulti);
            channel.setExtOutputStream(errorStreamMulti);
            channel.setErrStream(errorStream);
            channel.connect();
            boolean ok = waitForFinish(channel);
            if (!ok) {
                exception = new SSHException(TIMEOUT_MESSAGE);
            }
            exitStatus = channel.getExitStatus();
        } catch (JSchException e) {
            if (e.getMessage().contains("session is down")) {
                exception = new SSHException(TIMEOUT_MESSAGE, e);
            } else {
                exception = e;
            }
        } catch (Exception e) {
            exception = e;
        }
        String output = outputStream.toString(UTF_8);
        String error = errorStream.toString(UTF_8);

        return new Result(command, error, exception, exitStatus, output);
    }

    private boolean waitForFinish(ChannelExec channel) throws InterruptedException {

        final boolean[] timedOut = {false};
        Thread thread = new Thread(() -> {
            while (!channel.isClosed()) {
                if (timedOut[0]) {
                    return;
                }
                try {
                    Thread.sleep(RETRY_INTERVAL);
                } catch (InterruptedException ignored) {
                }
            }
        });
        thread.start();
        thread.join(timeoutMillis);

        if (thread.isAlive()) {
            timedOut[0] = true;
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return super.toString() +
                ", commands=" + commands +
                ", timeoutMillis=" + timeoutMillis;
    }

    /**
     * Gets dummy SSHExec.
     *
     * @return dummy SSHExec
     */
    public static SSHExec dummySSHExec() {
        return new DummySSHExec();
    }

    /**
     * Builder.
     *
     * @param cdkId      the CDK ID
     * @param remoteHost the remote host name or IP Address
     * @param <A>        the SSH type
     * @param <B>        the builder type
     * @return a new builder
     */
    @NonNull
    public static <A extends SSHExec<A>, B extends Builder<A, B>> Builder<A, B> builder(
            @NonNull String cdkId,
            @NonNull String remoteHost) {
        return new Builder<>(cdkId, remoteHost);
    }

    /**
     * Builder.
     *
     * @param <A> the action type
     * @param <B> the builder type
     */
    public static class Builder<A extends SSHExec<A>, B extends Builder<A, B>> extends SSHBase.Builder<A, B> {

        private final Map<String, Predicate<ExecutionContext>> commands = new LinkedHashMap<>();
        private int timeoutMillis;

        Builder(@NonNull String cdkId,
                @NonNull String remoteHost) {
            super(cdkId, remoteHost);
        }

        /**
         * Adds commands.
         *
         * @param commands commands to run
         * @return this
         */
        @NonNull
        public B commands(@NonNull List<String> commands) {
            for (String command : commands) {
                this.commands.put(command, null);
            }
            return self();
        }

        /**
         * Adds commands.
         *
         * @param commands  commands to run
         * @param condition condition to execute commands
         * @return this
         */
        @NonNull
        public B commands(@NonNull List<String> commands,
                          @NonNull Predicate<ExecutionContext> condition) {
            this.commands.putAll(commands
                    .stream()
                    .collect(Collectors.toMap(it -> it, it -> condition)));
            return self();
        }

        /**
         * Adds a command without an ExecutionContext predicate.
         *
         * @param command a command to run
         * @return this
         */
        @NonNull
        public B command(@NonNull String command) {
            commands.put(command, null);
            return self();
        }

        /**
         * Adds a command with an ExecutionContext predicate.
         *
         * @param command   a command to run
         * @param condition condition to execute command
         * @return this
         */
        @NonNull
        public B command(@NonNull String command, Predicate<ExecutionContext> condition) {
            commands.put(command, condition);
            return self();
        }

        /**
         * Timeout in milliseconds.
         *
         * @param timeoutMillis the number of milliseconds before the connection
         *                      will be dropped; defaults to 0, i.e. wait forever
         * @return this
         */
        @NonNull
        public B timeoutMillis(int timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
            return self();
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        protected A doBuild() throws SSHException {
            return (A) new SSHExec<>(
                    getCdkId(),
                    getStacks(),
                    getIpAddressReferences(),
                    commands,
                    getConnectTimeout(),
                    getKnownHostsPath(),
                    getPassphrase(),
                    getPassword(),
                    getPort(),
                    getPrivateKeyPath(),
                    getProxyHost(),
                    getProxyPort(),
                    getRemoteHost(),
                    getRetryDelay(),
                    getRetryMaxCount(),
                    getRetryMaxDelay(),
                    getRetryMultiplier(),
                    getServerAliveCountMax(),
                    getServerAliveIntervalSeconds(),
                    getSshConfigPath(),
                    timeoutMillis,
                    isTrustAllCertificates(),
                    getUsername());
        }
    }

    private class LoggerOutputStream extends OutputStream {

        private final StringBuilder sb = new StringBuilder();
        private final boolean stdout;

        LoggerOutputStream(boolean stdout) {
            this.stdout = stdout;
        }

        @Override
        public void write(int b) {
            if (b == '\n') {
                if (stdout) {
                    logger.info(sb.toString());
                } else {
                    logger.error(sb.toString());
                }
                sb.setLength(0);
            } else {
                sb.append((char) b);
            }
        }
    }

    private static class DummySSHExec extends SSHExec {

        DummySSHExec() throws SSHException {
            super("", List.of(), List.of(), getMap(), null, "", Secret.empty(), Secret.empty(), 80, "", null,
                    null, "", 0, 0, 0, 0, 1, 0, "", 0, true, "");
        }

        private static Map<String, Predicate<ExecutionContext>> getMap() {
            Map<String, Predicate<ExecutionContext>> map = new LinkedHashMap<>();
            map.put("", null);
            return map;
        }

        @Override
        protected void init(@NonNull ExecutionContext ec) {
        }

        @Override
        public List<Result> execute(@NonNull ExecutionContext ec) {
            return new ArrayList<>();
        }
    }
}
