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
import io.micronaut.core.annotation.NonNull;

import java.util.Optional;

/**
 * Used by DefaultIdResolver. Implementations find the associated CDK ID from
 * cloud ID or cloud ID from CDK ID, for a cloud and component combination,
 * e.g. OCI + Load balancer.
 */
public interface LookupIdResolver {

    /**
     * Find the CDK ID for the specified cloud ID and action.
     *
     * @param cloudId   the cloud ID
     * @param action    the action
     * @param component a retrieved instance
     * @return the CDK ID
     */
    @NonNull
    Optional<String> findCdkId(@NonNull String cloudId,
                               @NonNull Action<?> action,
                               @NonNull Object component);

    /**
     * Find the cloud ID for the specified CDK ID and action.
     *
     * @param cdkId  the CDK ID
     * @param action the action
     * @return the cloud ID
     */
    @NonNull
    Optional<String> findCloudId(@NonNull String cdkId,
                                 @NonNull Action<?> action);
}
