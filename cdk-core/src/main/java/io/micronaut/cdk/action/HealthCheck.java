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
package io.micronaut.cdk.action;

import io.micronaut.cdk.ExecutionContext;
import io.micronaut.cdk.action.ssh.SSHException;
import io.micronaut.cdk.component.IpAddressReference;
import io.micronaut.cdk.util.Assert;
import io.micronaut.core.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents an HTTP-based health check.
 *
 * @param <H> the type
 */
public class HealthCheck<H extends HealthCheck<H>> extends Executable<H> {

    private static final int DEFAULT_RETRY_COUNT = 5;

    private final String url;
    private final int retryCount;

    HealthCheck(@NonNull String cdkId,
                @NonNull Collection<String> stacks,
                @NonNull Collection<IpAddressReference> ipAddressReferences,
                @NonNull String url,
                int retryCount) {
        super(cdkId, stacks, ipAddressReferences);
        Assert.state(retryCount > 0 && retryCount < 1001, "Retry count must be between 1 - 1000. Default is 5.");
        this.url = Assert.notNull(url, "url cannot be null");
        this.retryCount = retryCount;
    }

    @Override
    public List<Result> execute(@NonNull ExecutionContext ec) {
        Assert.notNull(ec, "ExecutionContext cannot be null");

        String resolvedUrl = resolveIpAddresses(this.url, resolveIpAddresses(ec));
        resolvedUrl = resolveVariables(ec.getVariables(getCdkId()), resolvedUrl);
        logger.info("Health check '{}' calling url: {}", getCdkId(), resolvedUrl);

        int count = 0;
        int delay = 1000;

        while (count < retryCount) {
            count++;
            delay *= 2;
            if (delay > 30000) {
                delay = 30000;
            }

            Result result = get(resolvedUrl);
            if ((result.getException() != null && !(result.getException() instanceof IOException)) ||
                    result.getExitCode() == null ||
                    result.getExitCode() != 404) {
                logger.info("Health check '{}' result: {}", getCdkId(), result);
                return List.of(result);
            }

            logger.debug("Health check '{}' connect attempt {} failed , waiting {}ms to try again",
                    getCdkId(), count, delay);

            try {
                Thread.sleep(delay);
            } catch (InterruptedException ignored) {
                // ignored
            }
        }

        Result result = new Result(resolvedUrl,
                "ERROR: timeout, failed to get response in " + count + " attempts",
                null, null, "");
        logger.info("Health check '{}' result: {}", getCdkId(), result);
        return List.of(result);
    }

    private Result get(String resolvedUrl) {

        HttpURLConnection connection = null;
        StringBuilder response = new StringBuilder();
        Integer status = null;
        Exception exception = null;
        try {
            URL url = new URL(resolvedUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            status = connection.getResponseCode();
            if (status == 404 || status >= 500) {
                // retry if 404
                return new Result(resolvedUrl, "", null, status, "");
            }

            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
        } catch (Exception e) {
            if (e instanceof IOException) {
                // retry
                return new Result(resolvedUrl, "", e, 404, "");
            }
            exception = e;
        } finally {
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception ignored) {
                    // ignored
                }
            }
        }

        return new Result(resolvedUrl, "", exception, status, response.toString());
    }

    /**
     * Gets dummy HealthCheck.
     *
     * @return dummy HealthCheck
     */
    public static HealthCheck<?> dummyHealthCheck() {
        return new DummyHealthCheck<>();
    }

    /**
     * Builder.
     *
     * @param cdkId the CDK ID
     * @param <H>   the HealthCheck type
     * @param <B>   the builder type
     * @return a new builder
     */
    @NonNull
    public static <H extends HealthCheck<H>, B extends Builder<H, B>> Builder<H, B> builder(
            @NonNull String cdkId) {
        return new Builder<>(cdkId);
    }

    /**
     * Builder.
     *
     * @param <S> the HealthCheck type
     * @param <B> the builder type
     */
    public static class Builder<S extends HealthCheck<S>, B extends Builder<S, B>> extends Executable.Builder<S, B> {

        private String url;
        private int retryCount = DEFAULT_RETRY_COUNT;

        Builder(@NonNull String cdkId) {
            super(cdkId);
        }

        /**
         * URL.
         *
         * @param url the URL to invoke
         * @return this
         */
        @NonNull
        public B url(@NonNull String url) {
            this.url = Assert.hasText(url, "url is required");
            return self();
        }

        /**
         * Retry count, Value between 1 - 1000. Default is 5. After each retry delay time is doubled, until 30s which is maximum delay.
         *
         * @param retryCount the retryCount to invoke. Value between 1- 1000. Default is 5.
         * @return this
         */
        @NonNull
        public B retryCount(int retryCount) {
            Assert.state(retryCount > 0 && retryCount < 1001, "Retry count should be between 1 - 1000. Default is 5.");
            this.retryCount = retryCount;
            return self();
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        protected S doBuild() throws InvalidActionException {
            return (S) new HealthCheck<>(
                    getCdkId(),
                    getStacks(),
                    getIpAddressReferences(),
                    url,
                    retryCount);
        }
    }

    private static class DummyHealthCheck<D extends DummyHealthCheck<D>> extends HealthCheck<D> {

        DummyHealthCheck() throws SSHException {
            super("dummyHealthCheck", List.of(), List.of(), "", 1);
        }

        @Override
        public List<Result> execute(@NonNull ExecutionContext ec) {
            return new ArrayList<>();
        }
    }
}
