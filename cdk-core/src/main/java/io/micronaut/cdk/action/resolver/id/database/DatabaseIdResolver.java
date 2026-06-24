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
package io.micronaut.cdk.action.resolver.id.database;

import io.micronaut.cdk.action.Action;
import io.micronaut.cdk.action.IdResolver;
import io.micronaut.context.annotation.Secondary;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Named;

import java.util.Optional;

/**
 * IdResolver implementation that uses Micronaut Data JDBC.
 */
@Named("database")
@Secondary
public class DatabaseIdResolver implements IdResolver {

    private final CdkIdentityRepository repository;

    DatabaseIdResolver(CdkIdentityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<String> findCdkId(@NonNull String cloudId,
                                      @NonNull Action<?> action,
                                      @NonNull Object component) {
        return repository
                .findByCloudId(cloudId)
                .map(CdkIdPair::cdkId);
    }

    @Override
    public Optional<String> findCloudId(@NonNull String cdkId,
                                        @NonNull Action<?> action) {
        return repository
                .findByCdkId(cdkId)
                .map(CdkIdPair::cloudId);
    }

    @Override
    public void associate(@NonNull String cdkId,
                          @NonNull String cloudId) {
        if (repository.findByCdkId(cdkId).isEmpty()) {
            repository.save(new CdkIdPair(null, cdkId, cloudId));
        }
    }
}
