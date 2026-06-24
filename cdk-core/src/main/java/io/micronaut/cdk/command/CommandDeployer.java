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
package io.micronaut.cdk.command;

import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.component.Component;
import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ComponentService;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

/**
 * Deploys a resource based on a {@link Spec} contained in a {@link Command}.
 * A CommandDeployer must be registered using {@link ComponentService} against the
 * deployed {@link Component} class, and a {@link CloudSpecific} for the specific cloud.
 * Both annotations are required in order for a CommandDeployed implementation to be found.
 *
 * @param <S> spec type
 * @param <D> delete spec type
 * @param <N> cloud resource type
 * @param <C> cdk component type
 */
public interface CommandDeployer<S extends Spec<?>, D extends DeleteSpec<?>, N, C extends Component<N>> {

    /**
     * Deprecated, for transitional use only, will be never called. Having it as a default method allows
     * existing mixin interfaces to supply bridge methods to resource-named workers. The method will
     * be removed from the interface after all cloud supports are refactored.
     *
     * @param command deploy command
     * @return never completes normally
     * @deprecated
     */
    @Deprecated(forRemoval = true)
    default Component<?> deploy(@NonNull Command command) {
        throw new UnsupportedOperationException();
    }

    /**
     * Deprecated, for transitional use only, will be never called. Having it as a default method allows
     * existing mixin interfaces to supply bridge methods to resource-named workers. The method will
     * be removed from the interface after all cloud supports are refactored.
     *
     * @param command undeploy command
     * @return never completes normally
     * @deprecated
     */
    @Deprecated(forRemoval = true)
    default boolean undeploy(@NonNull Command command) {
        throw new UnsupportedOperationException();
    }

    /**
     * Deploys the command based on its spec if it's a supported type.
     *
     * @param command the command
     * @param spec    Spec of the resource to be deployed
     * @return a component corresponding to the command's spec, if deployed
     * @throws CommandRunException if there's a problem
     */
    @Nullable
    C deploy(@NonNull Command command, S spec) throws CommandRunException;

    /**
     * Un-deploys the command based on its spec if it's a supported type.
     *
     * @param command    the command
     * @param deleteSpec DeleteSpec for resource to be deleted.
     * @return true if undeployed
     * @throws CommandRunException if there's a problem
     */
    boolean undeploy(@NonNull Command command, D deleteSpec) throws CommandRunException;
}
