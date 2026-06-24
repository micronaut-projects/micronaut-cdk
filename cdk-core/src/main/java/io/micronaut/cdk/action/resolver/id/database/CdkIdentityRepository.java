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

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Base repository for CdkIdentity. Extend as needed and add
 * a <code>@JdbcRepository</code> annotation for your database type,
 * e.g. <code>@JdbcRepository(dialect = Dialect.ORACLE)</code>
 */
public interface CdkIdentityRepository extends CrudRepository<CdkIdPair, Long> {

    /**
     * Find a pair by CDK ID.
     *
     * @param cdkId CDK ID
     * @return the pair
     */
    Optional<CdkIdPair> findByCdkId(@NonNull String cdkId);

    /**
     * Find a pair by cloud ID.
     *
     * @param cloudId cloud ID
     * @return the pair
     */
    Optional<CdkIdPair> findByCloudId(@NonNull String cloudId);
}
