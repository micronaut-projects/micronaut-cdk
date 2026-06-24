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
package io.micronaut.cdk;

import io.micronaut.cdk.action.resolver.id.ResolverName;
import io.lettuce.core.RedisURI;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import io.micronaut.core.annotation.NonNull;

import java.net.URI;

/**
 * CDK config properties.
 */
@Context
@ConfigurationProperties(CdkConfigurationProperties.PREFIX)
public class CdkConfigurationProperties {

    /**
     * Prefix.
     */
    public static final String PREFIX = "cdk";

    private RetryConfigurationProperties retry = new RetryConfigurationProperties();
    private IdResolverConfigurationProperties idresolver = new IdResolverConfigurationProperties();
    private RedisConfigurationProperties redis = new RedisConfigurationProperties();
    private int threads = 10;

    /**
     * Constructor.
     */
    public CdkConfigurationProperties() {
    }

    /**
     * Retry configuration.
     *
     * @return retry configuration
     */
    public RetryConfigurationProperties getRetry() {
        return retry;
    }

    /**
     * Retry configuration.
     *
     * @param retry retry configuration
     */
    public void setRetry(RetryConfigurationProperties retry) {
        this.retry = retry;
    }

    /**
     * ID resolver configuration.
     *
     * @return ID resolver configuration
     */
    public IdResolverConfigurationProperties getIdresolver() {
        return idresolver;
    }

    /**
     * ID resolver configuration.
     *
     * @param idresolver ID resolver configuration
     */
    public void setIdresolver(IdResolverConfigurationProperties idresolver) {
        this.idresolver = idresolver;
    }

    /**
     * Redis configuration.
     *
     * @return Redis configuration
     */
    public RedisConfigurationProperties getRedis() {
        return redis;
    }

    /**
     * Redis configuration.
     *
     * @param redis Redis configuration
     */
    public void setRedis(RedisConfigurationProperties redis) {
        this.redis = redis;
    }

    /**
     * Number of threads for the ExecutorService used when deploying.
     *
     * @return number of threads
     */
    public int getThreads() {
        return threads;
    }

    /**
     * Number of threads for the ExecutorService used when deploying.
     *
     * @param threads number of threads
     */
    public void setThreads(int threads) {
        this.threads = threads;
    }

    @Override
    public String toString() {
        return "CdkConfigurationProperties{" +
                "retry=" + retry +
                ", idresolver=" + idresolver +
                ", redis=" + redis +
                ", threads=" + threads +
                '}';
    }

    /**
     * Retry configuration.
     */
    @ConfigurationProperties(RetryConfigurationProperties.PREFIX)
    public static class RetryConfigurationProperties {

        /**
         * Prefix.
         */
        public static final String PREFIX = "retry";

        /**
         * Default attempts.
         */
        public static final String DEFAULT_ATTEMPTS = "5";

        /**
         * Default delay.
         */
        public static final String DEFAULT_DELAY = "1s";

        /**
         * Default max delay.
         */
        public static final String DEFAULT_MAX_DELAY = "30s";

        /**
         * Default multiplier.
         */
        public static final String DEFAULT_MULTIPLIER = "2";

        private String attempts = DEFAULT_ATTEMPTS;
        private String delay = DEFAULT_DELAY;
        private String maxDelay = DEFAULT_MAX_DELAY;
        private String multiplier = DEFAULT_MULTIPLIER;

        /**
         * Constructor.
         */
        public RetryConfigurationProperties() {
        }

        /**
         * Maximum number of retry attempts.
         *
         * @return the maximum
         */
        public String getAttempts() {
            return attempts;
        }

        /**
         * Maximum number of retry attempts.
         *
         * @param attempts the maximum
         */
        public void setAttempts(String attempts) {
            this.attempts = attempts;
        }

        /**
         * Delay between retry attempts.
         *
         * @return the delay
         */
        public String getDelay() {
            return delay;
        }

        /**
         * Delay between retry attempts.
         *
         * @param delay the delay
         */
        public void setDelay(String delay) {
            this.delay = delay;
        }

        /**
         * Maximum overall delay.
         *
         * @return max delay
         */
        public String getMaxdelay() {
            return maxDelay;
        }

        /**
         * Maximum overall delay.
         *
         * @param maxDelay max delay
         */
        public void setMaxdelay(String maxDelay) {
            this.maxDelay = maxDelay;
        }

        /**
         * The multiplier to use to calculate the delay.
         *
         * @return the multiplier
         */
        public String getMultiplier() {
            return multiplier;
        }

        /**
         * The multiplier to use to calculate the delay.
         *
         * @param multiplier multiplier
         */
        public void setMultiplier(String multiplier) {
            this.multiplier = multiplier;
        }

        @Override
        public String toString() {
            return "RetryConfigurationProperties{" +
                    "attempts='" + attempts + '\'' +
                    ", delay='" + delay + '\'' +
                    ", maxDelay='" + maxDelay + '\'' +
                    ", multiplier='" + multiplier + '\'' +
                    '}';
        }
    }

    /**
     * IdResolver config.
     */
    @ConfigurationProperties(IdResolverConfigurationProperties.PREFIX)
    public static class IdResolverConfigurationProperties {

        /**
         * Prefix.
         */
        public static final String PREFIX = "idresolver";

        private ResolverName name = ResolverName.DEFAULT;
        private String className;

        /**
         * Constructor.
         */
        public IdResolverConfigurationProperties() {
        }

        /**
         * Name of one of the standard implementations of IdResolver.
         *
         * @return the name
         */
        public ResolverName getName() {
            return name;
        }

        /**
         * Name of one of the standard implementations of IdResolver.
         *
         * @param name the name
         */
        public void setName(@NonNull ResolverName name) {
            this.name = name;
        }

        /**
         * User-specified class name of an implementation of IdResolver to use.
         *
         * @return user-specified class name
         */
        public String getClassName() {
            return className;
        }

        /**
         * User-specified class name of an implementation of IdResolver to use.
         *
         * @param className full name and package of class that implements IdResolver
         */
        public void setClassName(@NonNull String className) {
            this.className = className;
        }

        @Override
        public String toString() {
            return "IdResolverConfigurationProperties{" +
                    "name=" + name +
                    ", className=" + className +
                    '}';
        }
    }

    /**
     * Redis Config.
     */
    @ConfigurationProperties(RedisConfigurationProperties.PREFIX)
    public static class RedisConfigurationProperties {

        /**
         * Prefix.
         */
        public static final String PREFIX = "redis";

        private RedisURI uri;

        /**
         * Constructor.
         */
        public RedisConfigurationProperties() {
        }

        /**
         * Redis URI.
         *
         * @return Redis URI
         */
        public RedisURI getUri() {
            return uri;
        }

        /**
         * Redis URI.
         *
         * @param uri Redis URI
         */
        public void setUri(@NonNull URI uri) {
            this.uri = RedisURI.create(uri);
        }

        @Override
        public String toString() {
            return "RedisConfigurationProperties{" +
                    "uri=" + uri +
                    '}';
        }
    }
}
