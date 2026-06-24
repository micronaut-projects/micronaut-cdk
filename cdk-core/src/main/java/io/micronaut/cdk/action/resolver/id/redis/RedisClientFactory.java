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
package io.micronaut.cdk.action.resolver.id.redis;

import io.micronaut.cdk.CdkConfigurationProperties;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;

/**
 * Creates Redis client and connection beans.
 */
@Factory
@Requires(property = "cdk.redis.uri")
@Requires(bean = CdkConfigurationProperties.class)
public class RedisClientFactory {

    RedisClientFactory() {
    }

    /**
     * Redis client.
     *
     * @param cdkConfigurationProperties configuration
     * @return Redis client
     */
    @Bean(preDestroy = "shutdown")
    @Singleton
    public RedisClient redisClient(@NonNull CdkConfigurationProperties cdkConfigurationProperties) {
        return RedisClient.create(cdkConfigurationProperties.getRedis().getUri());
    }

    /**
     * Redis connection.
     *
     * @param redisClient redis client
     * @return Redis connection
     */
    @Bean(preDestroy = "close")
    @Singleton
    public StatefulRedisConnection<String, String> redisConnection(@NonNull RedisClient redisClient) {
        return redisClient.connect();
    }
}
