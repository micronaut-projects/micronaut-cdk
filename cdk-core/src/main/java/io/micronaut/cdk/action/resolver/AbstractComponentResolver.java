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
package io.micronaut.cdk.action.resolver;

import io.micronaut.cdk.ExecutionContext;
import io.micronaut.cdk.action.ComponentResolver;
import io.micronaut.core.annotation.NonNull;

import java.util.Optional;

/**
 * Base class for component resolvers.
 * <p>
 * This class depends on an {@link ExecutionContext} to function correctly.
 * The {@code ExecutionContext} is injected automatically by the Micronaut dependency injection container
 * </p>
 *
 * @param <C> the cloud component type
 */
public abstract class AbstractComponentResolver<C> implements ComponentResolver<C> {

    /**
     * the execution context.
     */
    private final ExecutionContext ec;

    /**
     * Constructor.
     *
     * @param ec the execution context
     */
    protected AbstractComponentResolver(ExecutionContext ec) {
        this.ec = ec;
    }

    /**
     * Look for a component that's been resolved or deployed previously in the
     * current run to avoid unnecessary API call lookups.
     *
     * @param cdkId              the CDK ID
     * @param cloudComponentType the class of the cloud component in the <code>Component</code>
     * @return the cloud component if found
     */
    @NonNull
    protected Optional<C> findDeployed(@NonNull String cdkId,
                                       @NonNull Class<C> cloudComponentType) {
        return ec.findDeployed(cdkId, true, cloudComponentType);
    }
}
