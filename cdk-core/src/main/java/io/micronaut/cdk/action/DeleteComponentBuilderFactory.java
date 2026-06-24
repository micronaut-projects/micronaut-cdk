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

import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.component.Component;
import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ComponentService;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.UsedByGeneratedCode;

/**
 * Creates a DeleteSpec subclass Builder for an existing Component instance and a given cloud.
 * Implementations of this interface will be located in the ApplicationContext. They must be
 * qualified using {@link ComponentService} that identifies the handled Component
 * class or a superclass. They MAY be annotated with {@link CloudSpecific} to
 * create a DeleteSpec specific for a certain cloud.
 *
 * @param <D> delete spec type
 * @param <T> delete spec builder type
 * @param <N> cloud data type
 * @param <C> component type
 * @param <S> specification type
 */
@UsedByGeneratedCode
public interface DeleteComponentBuilderFactory<
        S extends Spec<S>,
        D extends DeleteSpec<D>,
        T extends DeleteSpec.Builder<D, T>,
        N,
        C extends Component<N>> {

    /**
     * Creates a {@link DeleteSpec.Builder} based on an existing {@link Component} instance and/or {@link Spec}
     * cloud resource data.
     * The implementation should create a {@link DeleteSpec.Builder} that deletes the resource passed as {@code component}
     * or specified by {@code spec}. The {@code spec} parameter may be missing; if the cloud resource does not contain
     * enough information to create the DeleteSpec builder, the implementation should throw {@link IllegalArgumentException}.
     * If the implementation does not want to handle the resource, it must return {@code null}.
     *
     * @param component the existing cloud resource.
     * @param spec      component spec
     * @return the Delete.Builder instance initialized to delete the Spec-ed resource
     */
    T deleteBuilder(@NonNull C component, S spec);
}
