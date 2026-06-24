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

import java.util.Collection;

/**
 * A provider that performs qualifier-based lookup in Micronaut application context
 * to select qualified services based on the specified {@link Action}.
 * <p>
 * Implementations of this interface allow retrieval of beans
 * (e.g. resolvers, deployers) that are annotated with
 * {@code @ComponentService}, {@code @CloudSpecific} qualifiers.
 */
public interface QualifiedServiceProvider {

    /**
     * Find all services of the given type that are qualified to work with
     * the specified {@link Action}.
     *
     * @param beanType The service type to search for (e.g. {@code LookupIdResolver}).
     * @param action   The action.
     * @param <T>      The bean type parameter.
     * @return A collection of services of the given type that
     * are qualified for the provided action. May be empty if
     * no services match.
     */
    @NonNull
    <T> Collection<T> findServices(Class<T> beanType, Action<?> action);
}

