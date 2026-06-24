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

import io.micronaut.cdk.Cloud;
import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.component.Component;
import io.micronaut.cdk.component.ComponentReference;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

import java.util.Collection;

/**
 * Service that converts between Specs and provides type information or other metadata.
 */
public interface ResourceSpecConverter {

    /**
     * Finds a Component represented by the given {@link DeleteSpec} instance. The method returns a Component subtype
     * Class object. If the passed DeleteSpec cannot be matched to any known Component, the method throws {@link UnsupportedOperationException}
     *
     * @param resourceSpec delete spec instance
     * @return type of the Component.
     */
    @NonNull
    Class<? extends Component<?>> findComponentClass(@NonNull DeleteSpec<?> resourceSpec);

    /**
     * Finds a Component represented by the given {@link Spec} instance. The method returns a Component subtype
     * Class object. If the passed Spec cannot be matched to any known Component, the method throws {@link UnsupportedOperationException}
     *
     * @param resourceSpec delete spec instance
     * @return type of the Component.
     */
    @NonNull
    Class<? extends Component<?>> findComponentClass(@NonNull Spec<?> resourceSpec);

    /**
     * Find a Spec type responsible for the given Component in the target cloud.
     *
     * @param component   component type
     * @param targetCloud the cloud
     * @return the spec type
     */
    @NonNull
    Class<? extends Spec<?>> findSpecClass(@NonNull Class<? extends Component> component, Cloud targetCloud);

    /**
     * Finds a DeleteSpec responsible for the given Component in the target cloud.
     *
     * @param component   component type
     * @param targetCloud the cloud
     * @return delete spec class
     */
    @NonNull
    Class<? extends DeleteSpec<?>> findDeleteSpecClass(@NonNull Class<? extends Component> component, Cloud targetCloud);

    /**
     * Makes and configures a DeleteSpec.Builder that deletes the passed Component. The DeleteSpec.Builder may
     * be specific to a cloud the Component is deployed in.
     *
     * @param existingComponent existing Component instance.
     * @param spec              the spec instance, can be {@code null}
     * @param <D>               the DeleteSpec type
     * @param <T>               the Builder type
     * @param <N>               the cloud data type
     * @param <C>               the Component type
     * @param <S>               the Spec type
     * @return initialized DeleteSpec.Builder instance.
     */
    @Nullable
    <S extends Spec<S>, D extends DeleteSpec<D>, T extends DeleteSpec.Builder<D, T>, N, C extends Component<N>> T deleteComponent(@NonNull C existingComponent, S spec);

    /**
     * Finds references to other components from the given Spec. The method
     * returns a collection of {@link ComponentReference}s configured into the given spec from
     * all, even optional, properties.
     *
     * @param spec  the spec instance
     * @param cloud the target cloud. Use {@link Cloud#ANY} for an unspecified cloud.
     * @param <S>   spec type.
     * @return list of references.
     */
    <S extends Spec<S>> @NonNull Collection<ComponentReference> findReferencedComponents(@NonNull S spec, Cloud cloud);
}
