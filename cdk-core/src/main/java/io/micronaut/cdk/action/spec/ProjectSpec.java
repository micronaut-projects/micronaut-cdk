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
package io.micronaut.cdk.action.spec;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.action.delete.ProjectDeleteSpec;
import io.micronaut.cdk.annotations.ResourceSpec;
import io.micronaut.cdk.component.Project;
import io.micronaut.core.annotation.NonNull;

import java.util.Collection;

/**
 * Project spec.
 *
 * @param <S> the spec type
 */
@ResourceSpec(component = Project.class)
public class ProjectSpec<S extends ProjectSpec<S>> extends Spec<S> {

    /**
     * Constructor.
     *
     * @param cdkId  CDK id
     * @param stacks stacks
     */
    protected ProjectSpec(@NonNull String cdkId,
                          @NonNull Collection<String> stacks) {
        super(cdkId, stacks);
    }

    /**
     * Creates a {@link ProjectDeleteSpec} based on an existing resource ProjectSpec.
     *
     * @param resource existing resource
     * @param <D>      DeleteSpec type
     * @param <B>      Builder type
     * @param <N>      Cloud object type
     * @param <C>      Resource component type
     * @return preconfigured Builder for the {@link ProjectDeleteSpec}
     */
    public static <D extends ProjectDeleteSpec<D>, B extends ProjectDeleteSpec.Builder<D, B>, N, C extends Project<N>> ProjectDeleteSpec.Builder<D, B> deleteBuilder(C resource) {
        return ProjectDeleteSpec.builder(resource);
    }

    /**
     * Builder.
     *
     * @param <S> the spec type
     * @param <B> the builder type
     */
    public abstract static class Builder<S extends ProjectSpec<S>, B extends Builder<S, B>> extends Spec.Builder<S, B> {

        /**
         * Constructor.
         *
         * @param cdkId CDK ID
         */
        protected Builder(@NonNull String cdkId) {
            super(cdkId);
        }

        @Override
        protected void validate() throws InvalidActionException {
            super.validate();
        }
    }
}
