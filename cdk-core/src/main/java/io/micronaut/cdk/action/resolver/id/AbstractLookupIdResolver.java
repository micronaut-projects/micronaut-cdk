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

import io.micronaut.cdk.ExecutionContext;
import io.micronaut.cdk.action.Action;
import io.micronaut.cdk.component.Component;
import io.micronaut.core.annotation.NonNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Base class for ID resolvers.
 * <p>
 * This class depends on an {@link ExecutionContext} to function correctly.
 * The {@code ExecutionContext} is injected automatically by the Micronaut dependency injection container
 * </p>
 */
public abstract class AbstractLookupIdResolver implements LookupIdResolver {

    private final ExecutionContext ec;

    /**
     * Constructor.
     *
     * @param ec execution context
     */
    protected AbstractLookupIdResolver(ExecutionContext ec) {
        this.ec = ec;
    }

    @Override
    public Optional<String> findCdkId(@NonNull String cloudId,
                                      @NonNull Action<?> action,
                                      @NonNull Object component) {
        return doFindCdkId(cloudId, action, component);
    }

    /**
     * Find the CDK ID for the specified cloud ID and action.
     *
     * @param cloudId   the cloud ID
     * @param action    the action
     * @param component a retrieved instance
     * @return the CDK ID
     */
    protected abstract Optional<String> doFindCdkId(@NonNull String cloudId,
                                                    @NonNull Action<?> action,
                                                    @NonNull Object component);

    /**
     * Find the cloud ID for the specified CDK ID and action.
     *
     * @param cdkId  the CDK ID
     * @param action the action
     * @return the cloud ID
     */
    @Override
    public Optional<String> findCloudId(@NonNull String cdkId,
                                        @NonNull Action<?> action) {
        return doFindCloudId(cdkId, action);
    }

    /**
     * Find the cloud ID for the specified CDK ID and action.
     *
     * @param cdkId  CDK ID
     * @param action the action
     * @return the cloud ID
     */
    protected abstract Optional<String> doFindCloudId(@NonNull String cdkId,
                                                      @NonNull Action<?> action);

    /**
     * Look for a component that's been resolved or deployed previously in the
     * current run, or use the supplier to find the component, to avoid
     * unnecessary API call lookups.
     *
     * @param cdkId              the CDK ID
     * @param cloudComponentType the class of the cloud component in the <code>Component</code>
     * @param finder             lookup supplier, will use API calls
     * @param componentCreator   function to create a Component to register as resolved in the ExecutionContext
     * @param <C>                the type of the cloud component
     * @return the cloud component if found
     */
    @NonNull
    protected <C> Optional<C> find(@NonNull String cdkId,
                                   @NonNull Class<C> cloudComponentType,
                                   @NonNull Supplier<Optional<C>> finder,
                                   @NonNull Function<C, ? extends Component<?>> componentCreator) {

        Optional<C> deployed = ec.findDeployed(cdkId, true, cloudComponentType);
        if (deployed.isPresent()) {
            return deployed;
        }

        Optional<C> found = finder.get();
        if (found.isPresent()) {
            ec.registerResolved(componentCreator.apply(found.get()));
            return found;
        }

        return Optional.empty();
    }
}
