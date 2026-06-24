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

import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.component.Component;
import io.micronaut.core.annotation.NonNull;

/**
 * Determines if there's a diff between data in the spec and component that
 * requires an update.
 */
@FunctionalInterface
public interface UpdateResolver {

    /**
     * Determine if the component needs to be updated.
     *
     * @param spec      the spec
     * @param component the component
     * @return true if update needed
     */
    boolean needsUpdate(@NonNull Spec<?> spec,
                        @NonNull Component<?> component);
}
