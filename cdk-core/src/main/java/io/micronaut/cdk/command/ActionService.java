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

import io.micronaut.cdk.action.Action;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.action.delete.DeleteSpec;
import jakarta.inject.Qualifier;

/**
 * Qualifier that binds a bean to an Action subclass like {@link Spec} or {@link DeleteSpec}.
 */
@Qualifier
public @interface ActionService {
    /**
     * Action type this bean is associated with.
     *
     * @return action type.
     */
    @SuppressWarnings("rawtypes")
    Class<? extends Action> value();
}
