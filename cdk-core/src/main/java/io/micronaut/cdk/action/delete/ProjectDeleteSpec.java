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
package io.micronaut.cdk.action.delete;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.component.ComponentReference;
import io.micronaut.cdk.component.Project;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

import java.util.Collection;

/**
 * Un-deploy by CDK ID or cloud ID.
 *
 * @param <D> the delete spec type
 */
public class ProjectDeleteSpec<D extends ProjectDeleteSpec<D>> extends DeleteSpec<D> {

    ProjectDeleteSpec(@NonNull String id,
                      @NonNull Collection<String> stacks,
                      boolean byCdkId,
                      boolean errorIfNotFound,
                      @Nullable String newName,
                      @Nullable ComponentReference parentReference) {
        super(id, Project.class, stacks, byCdkId, errorIfNotFound, newName, parentReference);
    }

    /**
     * Creates a builder for ProjectDeleteSpec.
     *
     * @param id      the id
     * @param byCdkId true if the id is a CDK ID, false if a cloud ID
     * @param <D>     the delete spec type
     * @param <B>     the builder type
     * @return a new builder
     */
    @NonNull
    public static <D extends ProjectDeleteSpec<D>, B extends Builder<D, B>> Builder<D, B> builder(
            @NonNull String id, boolean byCdkId) {
        return new Builder<>(id, byCdkId);
    }

    /**
     * Creates a builder for ProjectDeleteSpec.
     *
     * @param resource the resource spec
     * @param <D>      the delete spec type
     * @param <B>      the builder type
     * @param <N>      the cloud object type
     * @param <C>      the component type
     * @return a new builder
     */
    public static <D extends ProjectDeleteSpec<D>, B extends Builder<D, B>, N, C extends Project<N>> Builder<D, B> builder(C resource) {
        return new Builder<>(resource.getCloudId(), false);
    }

    /**
     * Builder.
     *
     * @param <D> the spec type
     * @param <B> the builder type
     */
    public static class Builder<D extends ProjectDeleteSpec<D>, B extends Builder<D, B>> extends DeleteSpec.Builder<D, B> {

        Builder(@NonNull String id, boolean byCdkId) {
            super(id, byCdkId, Project.class);
        }

        @Override
        @NonNull
        @SuppressWarnings("unchecked")
        public D doBuild() throws InvalidActionException {
            validate();
            return (D) new ProjectDeleteSpec<>(
                    getId(),
                    getStacks(),
                    isByCdkId(),
                    isErrorIfNotFound(),
                    getNewName(),
                    getParentReference()
            );
        }

        @Override
        protected void validate() throws InvalidActionException {
            super.validate();
        }
    }
}
