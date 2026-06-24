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

import io.micronaut.cdk.action.Action;
import io.micronaut.cdk.action.IdResolver;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.micronaut.context.annotation.Secondary;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Named;

import java.util.Optional;

/**
 * ID resolver for Redis.
 */
@Named("redis")
@Secondary
public class RedisIdResolver implements IdResolver {

    private static final String NAMESPACE = "cdk:";

    private final RedisCommands<String, String> commands;

    RedisIdResolver(@NonNull StatefulRedisConnection<String, String> connection) {
        commands = connection.sync();
    }

    @Override
    @NonNull
    public Optional<String> findCdkId(@NonNull String cloudId,
                                      @NonNull Action<?> action,
                                      @NonNull Object component) {
        return Optional.ofNullable(commands.get(NAMESPACE + cloudId));
    }

    @Override
    @NonNull
    public Optional<String> findCloudId(@NonNull String cdkId,
                                        @NonNull Action<?> action) {
        return Optional.ofNullable(commands.get(NAMESPACE + cdkId));
    }

    @Override
    public void associate(String cdkId, String cloudId) {
        commands.set(NAMESPACE + cdkId, cloudId);
        commands.set(NAMESPACE + cloudId, cdkId);
    }
}
