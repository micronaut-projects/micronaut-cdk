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
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends files using SCP.
 * <p>
 * Based on code from org.apache.tools.ant.taskdefs.optional.ssh.
 *
 * @param <A> the type
 */
public class Scp<A extends Scp<A>> extends SSHBase<A> {

    private static final byte[] ACK = new byte[1];
    private static final int BUFFER_SIZE = 100 * 1024;
    private static final int DEFAULT_FILE_MODE = 0644;
    private static final double ONE_SECOND = 1000;

    private final boolean compress;
    private final Map<File, String> files;
    private final int fileMode;
    private final boolean preserveLastModified;

    /**
     * Constructor.
     *
     * @param cdkId                      CDK ID
     * @param stacks                     stacks
     * @param ipAddressReferences        IP address references
     * @param compress                   whether to use compression
     * @param connectTimeout             connect timeout seconds
     * @param files                      the files and remote paths
     * @param fileMode                   the file mode
     * @param knownHostsPath             location of known_hosts, e.g. ~/.ssh/known_hosts
     * @param passphrase                 the private key passphrase
     * @param password                   the password
     * @param port                       SSH port
     * @param preserveLastModified       whether to set the remote last modified date from the local file date
     * @param privateKeyPath             location of the file containing the private key, e.g. ~/.ssh/id_rsa
     * @param proxyHost                  HTTP proxy host
     * @param proxyPort                  HTTP proxy port
     * @param remoteHost                 remote host
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
    protected Scp(@NonNull String cdkId,
                  @NonNull Collection<String> stacks,
                  @NonNull Collection<IpAddressReference> ipAddressReferences,
                  boolean compress,
                  @Nullable Integer connectTimeout,
                  @NonNull Map<File, String> files,
                  int fileMode,
                  @Nullable String knownHostsPath,
                  @NonNull Secret<String> passphrase,
                  @NonNull Secret<String> password,
                  int port,
                  boolean preserveLastModified,
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
        super(cdkId, stacks, ipAddressReferences, connectTimeout, knownHostsPath, passphrase, password,
                port, privateKeyPath, proxyHost, proxyPort, remoteHost, retryDelay, retryMaxCount,
                retryMaxDelay, retryMultiplier, serverAliveCountMax, serverAliveIntervalSeconds,
                sshConfigPath, trustAllCertificates, username);
        Assert.notNull(files, "files cannot be null");
        Assert.state(!files.isEmpty(), "at least one File is required");
        this.compress = compress;
        this.files = files;
        this.fileMode = fileMode;
        this.preserveLastModified = preserveLastModified;
    }

    @Override
    public List<Result> execute(@NonNull ExecutionContext ec) {

        init(Assert.notNull(ec, "ExecutionContext cannot be null"));

        List<Result> results = new ArrayList<>(files.size());

        Session session = null;
        try {
            session = openSession();
            for (Map.Entry<File, String> entry : files.entrySet()) {
                Result result = sendFile(entry.getKey(), entry.getValue(), session);
                results.add(result);
                if (result.getException() != null) {
                    break;
                }
            }
        } catch (Exception e) {
            if (e instanceof SSHException) {
                throw (SSHException) e;
            }
            throw new SSHException(e);
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }

        return results;
    }

    private Result sendFile(@NonNull File file,
                            @NonNull String remotePath,
                            @NonNull Session session) {

        String command = "scp -t ";
        if (preserveLastModified) {
            command += "-p ";
        }
        if (compress) {
            command += "-C ";
        }
        command += remotePath;

        Exception exception = null;

        Channel channel = null;
        try {
            channel = openExecChannel(command, session);
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();
            waitForAck(in);

            sendFile(file, in, out);
        } catch (Exception e) {
            exception = e;
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }

        return new Result("scp " + file + " -> " + remotePath,
                "", exception, 0, "");
    }

    private void sendFile(@NonNull File file,
                          @NonNull InputStream in,
                          @NonNull OutputStream out)
            throws IOException, SSHException {

        long filesize = file.length();

        if (preserveLastModified) {
            String command = "T" + (file.lastModified() / 1000) + " 0" +
                    ' ' + (file.lastModified() / 1000) + " 0\n";
            out.write(command.getBytes());
            out.flush();
            waitForAck(in);
        }

        // send "C0644 filesize filename"
        String command = "C0" + Integer.toOctalString(fileMode) +
                ' ' + filesize + ' ' +
                file.getName() + '\n';
        out.write(command.getBytes());
        out.flush();
        waitForAck(in);

        byte[] buffer = new byte[BUFFER_SIZE];
        long startTime = System.currentTimeMillis();
        long totalSent = 0;

        boolean trackProgress = logger.isDebugEnabled();
        int percent = 0;

        try (InputStream fis = Files.newInputStream(file.toPath())) {
            logger.info("Sending: {} : {}", file.getName(), file.length());
            while (true) {
                int len = fis.read(buffer, 0, buffer.length);
                if (len <= 0) {
                    break;
                }
                out.write(buffer, 0, len);
                totalSent += len;

                if (trackProgress) {
                    percent = trackProgress(filesize, totalSent, percent);
                }
            }

            out.flush();
            sendAck(out);
            waitForAck(in);

        } finally {
            logStats(startTime, totalSent);
        }
    }

    private Channel openExecChannel(@NonNull String command,
                                    @NonNull Session session) throws JSchException {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        return channel;
    }

    private void sendAck(@NonNull OutputStream out) throws IOException {
        out.write(ACK);
        out.flush();
    }

    private void waitForAck(@NonNull InputStream in)
            throws IOException, SSHException {

        int b = in.read();

        if (b == -1) {
            throw new SSHException("No response from server");
        }

        if (b == 0) {
            return; // success
        }

        StringBuilder sb = new StringBuilder();

        int c = in.read();
        while (c > 0 && c != '\n') {
            sb.append((char) c);
            c = in.read();
        }

        if (b == 1) {
            throw new SSHException("server indicated an error: " + sb);
        }

        if (b == 2) {
            throw new SSHException("server indicated a fatal error: " + sb);
        }

        throw new SSHException("unknown response, code " + b + " message: " + sb);
    }

    private void logStats(long timeStarted, long totalSent) {
        if (logger.isDebugEnabled()) {
            double duration = (System.currentTimeMillis() - timeStarted) / ONE_SECOND;
            NumberFormat format = NumberFormat.getNumberInstance();
            format.setMaximumFractionDigits(2);
            format.setMinimumFractionDigits(1);
            logger.debug("File transfer time: {} Average Rate: {} B/s",
                    format.format(duration), format.format(totalSent / duration));
        }
    }

    private int trackProgress(long filesize, long totalSent, int previousPercent) {

        int percent = (int) Math.round(Math.floor((totalSent / (double) filesize) * 100));

        if (percent > previousPercent) {
            if (percent == 100) {
                logger.debug("100%");
            } else if (percent % 10 == 0) {
                logger.debug(" {}%", percent);
            }
        }

        return percent;
    }

    @Override
    public String toString() {
        return super.toString() +
                ", compress=" + compress +
                ", files=" + files +
                ", fileMode=" + fileMode +
                ", preserveLastModified=" + preserveLastModified;
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
    public static <A extends Scp<A>, B extends Builder<A, B>> Builder<A, B> builder(
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
    public static class Builder<A extends Scp<A>, B extends Builder<A, B>> extends SSHBase.Builder<A, B> {

        private final Map<File, String> files = new LinkedHashMap<>();

        private boolean compress;
        private int fileMode = DEFAULT_FILE_MODE;
        private boolean preserveLastModified;

        /**
         * Constructor.
         *
         * @param cdkId      CDK ID
         * @param remoteHost remote host URL or IP address
         */
        protected Builder(@NonNull String cdkId,
                          @NonNull String remoteHost) {
            super(cdkId, remoteHost);
        }

        /**
         * Whether to use compression.
         *
         * @param compress true to use compression
         * @return this
         */
        @NonNull
        public B compress(boolean compress) {
            this.compress = compress;
            return self();
        }

        /**
         * Add a file.
         *
         * @param file       the file
         * @param remotePath the remote path
         * @return this
         */
        @NonNull
        public B file(@NonNull File file,
                      @NonNull String remotePath) {
            files.put(file, remotePath);
            return self();
        }

        /**
         * Add files.
         *
         * @param files the files and remote paths
         * @return this
         */
        @NonNull
        public B files(@NonNull Map<File, String> files) {
            this.files.putAll(files);
            return self();
        }

        /**
         * File mode.
         *
         * @param fileMode the file mode
         * @return this
         */
        @NonNull
        public B fileMode(@NonNull String fileMode) {
            return fileMode(Integer.parseInt(fileMode, 8));
        }

        /**
         * File mode.
         *
         * @param fileMode the file mode
         * @return this
         */
        @NonNull
        public B fileMode(int fileMode) {
            this.fileMode = fileMode;
            return self();
        }

        /**
         * Preserve last modified.
         *
         * @param preserveLastModified whether to set the remote last modified
         *                             date from the local file date
         * @return this
         */
        @NonNull
        public B preserveLastModified(boolean preserveLastModified) {
            this.preserveLastModified = preserveLastModified;
            return self();
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        protected A doBuild() throws SSHException {
            return (A) new Scp<>(
                    getCdkId(),
                    getStacks(),
                    getIpAddressReferences(),
                    compress,
                    getConnectTimeout(),
                    files,
                    fileMode,
                    getKnownHostsPath(),
                    getPassphrase(),
                    getPassword(),
                    getPort(),
                    preserveLastModified,
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
                    isTrustAllCertificates(),
                    getUsername());
        }
    }
}
