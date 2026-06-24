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
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

import java.util.Collection;
import java.util.Objects;

/**
 * Un-deploy by CDK ID or cloud ID.
 *
 * @param <D> the delete spec type
 */
public class FunctionDeleteSpec<D extends FunctionDeleteSpec<D>> extends DeleteSpec<D> {

    /**
     * @param id                   id
     * @param stacks               stacks
     * @param byCdkId              {@code true} if id is a CDK ID, false if a cloud ID
     * @param errorIfNotFound      if {@code true} throw an exception if the specified
     *                             instance doesn't exist
     * @param newName              optional new name
     * @param applicationReference application reference
     */
    FunctionDeleteSpec(@NonNull String id,
                       @NonNull Collection<String> stacks,
                       boolean byCdkId,
                       boolean errorIfNotFound,
                       @Nullable String newName,
                       @NonNull ComponentReference applicationReference) {
        super(id, stacks, byCdkId, errorIfNotFound, newName, applicationReference);
    }

    /**
     * Application reference.
     *
     * @return the reference
     */
    @NonNull
    public ComponentReference getApplicationReference() {
        return Objects.requireNonNull(getParentReference());
    }

    /**
     * Builder.
     *
     * @param id                   the id
     * @param byCdkId              true if the id is a CDK ID, false if a cloud ID
     * @param applicationReference the application reference
     * @param <D>                  the delete spec type
     * @param <B>                  the builder type
     * @return a new builder
     */
    @NonNull
    public static <D extends FunctionDeleteSpec<D>, B extends Builder<D, B>> Builder<D, B> builder(
            @NonNull String id,
            boolean byCdkId,
            @NonNull ComponentReference applicationReference) {
        return new Builder<>(id, byCdkId, applicationReference);
    }

    /**
     * Builder.
     *
     * @param <D> the spec type
     * @param <B> the builder type
     */
    public static class Builder<D extends FunctionDeleteSpec<D>, B extends Builder<D, B>> extends DeleteSpec.Builder<D, B> {

        /**
         * Constructor.
         *
         * @param id                   id
         * @param byCdkId              {@code true} if id is a CDK ID, false if a cloud ID
         * @param applicationReference application reference
         */
        protected Builder(@NonNull String id,
                          boolean byCdkId,
                          @NonNull ComponentReference applicationReference) {
            super(id, byCdkId);
            parentReference(applicationReference);
        }

        @Override
        @NonNull
        @SuppressWarnings("unchecked")
        protected D doBuild() throws InvalidActionException {
            return (D) new FunctionDeleteSpec<>(
                    getId(),
                    getStacks(),
                    isByCdkId(),
                    isErrorIfNotFound(),
                    getNewName(),
                    requireReference("applicationReference", getParentReference())
            );
        }

        @Override
        protected void validate() throws InvalidActionException {
            super.validate();

            requireReference("applicationReference", getParentReference());
        }
    }
}
