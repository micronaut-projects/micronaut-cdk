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
import io.micronaut.cdk.component.ComponentReference;

/**
 * Specifies a relationship to an action taken on a certain Component. The dependency describes
 * the lifecycle state of the Component that must be reached, and the order of the dependent
 * action. If {@code lifecycle} is {@link Lifecycle#CREATE} and {@code order} is {@link Order#AFTER}
 * this Dependency asserts that an action should be performed <b>after</b> the referenced component
 * is created.
 * <p>
 * These Dependency descriptors will be turned into {@link Action} dependencies by the
 * infrastructure. The provider does not need to know the exact Action instance, it is identified by
 * ID or CDK ID of the {@link ComponentReference}.
 *
 * @param reference reference to a component
 * @param lifecycle state of the component
 * @param order     relative order of the dependent Action relative to the referenced Component.
 */
public record ComponentDependency(ComponentReference reference, Lifecycle lifecycle, Order order) {
    /**
     * The state of the referenced Component.
     */
    enum Lifecycle {
        CREATE, DELETE, UPDATE
    }

    /**
     * Relative ordering for the action.
     */
    enum Order {
        BEFORE, AFTER
    }
}
