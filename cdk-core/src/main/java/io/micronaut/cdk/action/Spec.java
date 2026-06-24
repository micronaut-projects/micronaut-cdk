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
import io.micronaut.cdk.component.ComponentReference;
import io.micronaut.core.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Base class for specs.
 *
 * @param <S> the spec type
 */
public abstract class Spec<S extends Spec<S>> extends Action<S> {
    Class<? extends Component> componentClass;

    /**
     * Constructor.
     *
     * @param cdkId  CDK ID
     * @param stacks stacks
     */
    protected Spec(@NonNull String cdkId,
                   @NonNull Collection<String> stacks) {
        super(cdkId, stacks);
    }

    protected static List<ComponentReference> getComponentReferences(Object... refs) {
        List<ComponentReference> refList = new ArrayList<>();
        for (Object o : refs) {
            if (o instanceof ComponentReference cr) {
                refList.add(cr);
            } else if (o instanceof Collection c) {
                if (!c.isEmpty()) {
                    Object first = c.iterator().next();
                    if (!(first instanceof ComponentReference cr)) {
                        throw new IllegalArgumentException("Component references must be of type ComponentReference");
                    }
                    refList.addAll((Collection<ComponentReference>) c);
                }
            }
        }
        return refList;
    }

    /**
     * Sets the deployed component class. The class can be set only once (or to the same value as previously).
     * a Specs' class can not change.
     *
     * @param componentClass the component class.
     * @return this instance.
     */
    protected final S setComponentClass(Class<? extends Component> componentClass) {
        if (this.componentClass != null && this.componentClass != componentClass) {
            throw new IllegalStateException("Component class is already set to " + this.componentClass +
                    ", can not be changed to " + componentClass);
        }
        this.componentClass = componentClass;
        return (S) this;
    }

    /**
     * Gets the class of the Component that will be deployed from this Spec.
     *
     * @return component class.
     */
    public Class<? extends Component> getComponentClass() {
        return componentClass;
    }

    /**
     * Builder.
     *
     * @param <S> the spec type
     * @param <B> the builder type
     */
    public abstract static class Builder<S extends Spec<S>, B extends Builder<S, B>> extends Action.Builder<S, B> {

        /**
         * Constructor.
         *
         * @param cdkId CDK ID
         */
        protected Builder(@NonNull String cdkId) {
            super(cdkId);
        }

        /**
         * Remove null elements.
         *
         * @param collection the collection
         * @param <T>        the collection type
         * @return the collection without nulls
         */
        protected <T> Collection<T> removeNulls(@NonNull Collection<T> collection) {
            return collection.stream().filter(Objects::nonNull).toList();
        }

        /**
         * Remove null elements.
         *
         * @param list the list
         * @param <T>  the list type
         * @return the list without nulls
         */
        protected <T> List<T> removeNulls(@NonNull List<T> list) {
            return list == null ? null : list.stream().filter(Objects::nonNull).toList();
        }
    }
}
