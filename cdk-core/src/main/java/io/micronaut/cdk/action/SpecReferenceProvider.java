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

import io.micronaut.cdk.component.ComponentReference;
import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ComponentService;

import java.util.Collection;

/**
 * Provides list of references defined by a Spec. The interface may be implemented on Spec itself,
 * in which case it must be used in preference to other sources. Implementations may be registered
 * with {@link ComponentService}, {@link CloudSpecific} qualifiers
 * for certain components only.
 *
 * @param <S> spec type.
 */
public interface SpecReferenceProvider<S extends Spec<?>> {
    /**
     * Identifies component references used by the Spec.
     *
     * @param spec resource Spec instance
     * @return list of referenced other Components.
     */
    Collection<ComponentReference> findReferences(S spec);
}
