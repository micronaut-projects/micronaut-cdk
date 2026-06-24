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
package io.micronaut.cdk.graph;

import io.micronaut.cdk.command.Commands;
import io.micronaut.context.annotation.DefaultImplementation;
import io.micronaut.core.annotation.NonNull;

/**
 * Sorts nodes into the order they can be processed.
 */
@FunctionalInterface
@DefaultImplementation(DefaultDependencyResolver.class)
public interface DependencyResolver {

    /**
     * Sorts commands based on their actions' dependencies.
     *
     * @param commands the commands
     * @return a new {@link Commands} instance where the backing collection is a List and the elements are sorted
     * @throws DependencyCycleException if there's a cycle
     */
    @NonNull
    Commands sort(@NonNull Commands commands) throws DependencyCycleException;
}
