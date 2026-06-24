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

import io.micronaut.core.annotation.NonNull;

import java.util.Optional;

/**
 * Associates a CDK ID with a cloud component ID.
 */
public interface IdResolver {

    /**
     * Find the CDK ID associated with the specified cloud ID and action.
     *
     * @param cloudId   the cloud ID
     * @param action    the action
     * @param component cloud component
     * @return the CDK ID
     */
    @NonNull
    Optional<String> findCdkId(@NonNull String cloudId,
                               @NonNull Action<?> action,
                               @NonNull Object component);

    /**
     * Find the cloud ID associated with the specified CDK ID and action.
     *
     * @param cdkId  the CDK ID
     * @param action the action
     * @return the cloud ID
     */
    @NonNull
    Optional<String> findCloudId(@NonNull String cdkId,
                                 @NonNull Action<?> action);

    /**
     * Store an association between the CDK ID and cloud ID.
     *
     * @param cdkId   the CDK ID
     * @param cloudId the cloud ID
     */
    void associate(@NonNull String cdkId,
                   @NonNull String cloudId);
}
