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
package io.micronaut.cdk.delta;

import io.micronaut.context.annotation.DefaultImplementation;

/**
 * Calculates the difference between existing components and actions.
 */
@FunctionalInterface
@DefaultImplementation(DefaultDeltaResolver.class)
public interface DeltaResolver {

    /**
     * Builds a {@link Delta} and sets it in the context.
     *
     * @throws DeltaResolutionException if there's a problem
     */
    void resolve() throws DeltaResolutionException;
}
