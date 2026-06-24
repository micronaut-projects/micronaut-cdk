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
package io.micronaut.cdk.impl;

import io.micronaut.cdk.Cloud;
import io.micronaut.cdk.component.Component;

/**
 * Trampoline that can be used by infrastructure to set Component properties without setters
 * in the Component public interface.
 */
public abstract class ComponentAccessor {

    private static ComponentAccessor instance;

    /**
     * Creates an accessor, and sets an instance.
     */
    protected ComponentAccessor() {
        if (ComponentAccessor.instance != null) {
            throw new IllegalStateException("Component instance already set");
        }
        ComponentAccessor.instance = this;
    }

    /**
     * Sets the Cloud property to the specified Component.
     *
     * @param c     the component
     * @param cloud value of cloud property.
     */
    public abstract void setCloud(Component<?> c, Cloud cloud);

    /**
     * Returns the instance of the Accessor.
     *
     * @return the instance to use.
     */
    public static ComponentAccessor getInstance() {
        return instance;
    }
}
