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

import io.micronaut.cdk.component.Component;
import io.micronaut.cdk.component.ExistingComponents;
import io.micronaut.context.annotation.DefaultImplementation;
import io.micronaut.core.annotation.NonNull;

import java.util.Optional;

/**
 * Determines existing {@link Component}s associated with {@link Action}s.
 */
@DefaultImplementation(DefaultExistingComponentResolver.class)
public interface ExistingComponentResolver {

    /**
     * Resolves {@link ExistingComponents} and stores in the context.
     *
     * @param actions actions to process
     * @return resolved components.
     */
    ExistingComponents resolve(@NonNull Iterable<Action<?>> actions) throws ComponentResolverException;

    /**
     * Find the existing component corresponding to the action.
     *
     * @param action the action
     * @return the component if found
     */
    @NonNull
    Optional<Component<?>> findExisting(@NonNull Action<?> action);
}
