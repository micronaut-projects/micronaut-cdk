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
package io.micronaut.cdk.action.resolver.id;

import io.micronaut.cdk.action.Action;
import io.micronaut.cdk.action.IdResolver;
import io.micronaut.cdk.action.QualifiedServiceProvider;
import io.micronaut.context.annotation.Secondary;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Named;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Default implementation that uses cloud provider tags for CDK ID.
 */
@Named("default")
@Secondary
public class DefaultIdResolver implements IdResolver {

    private final QualifiedServiceProvider serviceProvider;
    private final ConcurrentMap<String, String> cdkIdByCloudId = new ConcurrentHashMap<>();

    DefaultIdResolver(QualifiedServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @Override
    public synchronized Optional<String> findCdkId(@NonNull String cloudId,
                                                   @NonNull Action<?> action,
                                                   @NonNull Object component) {

        if (cdkIdByCloudId.containsKey(cloudId)) {
            return Optional.of(cdkIdByCloudId.get(cloudId));
        }

        Collection<LookupIdResolver> resolvers = serviceProvider.findServices(LookupIdResolver.class, action);

        for (var resolver : resolvers) {
            Optional<String> cdkId = resolver.findCdkId(cloudId, action, component);
            if (cdkId.isPresent()) {
                associate(cdkId.get(), cloudId);
                return cdkId;
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<String> findCloudId(@NonNull String cdkId,
                                        @NonNull Action<?> action) {

        Collection<LookupIdResolver> resolvers = serviceProvider.findServices(LookupIdResolver.class, action);

        for (var resolver : resolvers) {
            Optional<String> cloudId = resolver.findCloudId(cdkId, action);
            if (cloudId.isPresent()) {
                associate(cdkId, cloudId.get());
                return cloudId;
            }
        }

        return Optional.empty();
    }

    @Override
    public void associate(@NonNull String cdkId,
                          @NonNull String cloudId) {
        cdkIdByCloudId.put(cloudId, cdkId);
    }
}
