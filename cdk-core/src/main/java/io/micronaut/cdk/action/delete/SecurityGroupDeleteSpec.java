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

/**
 * Un-deploy by CDK ID or cloud ID.
 *
 * @param <D> the delete spec type
 */
public class SecurityGroupDeleteSpec<D extends SecurityGroupDeleteSpec<D>> extends DeleteSpec<D> {

    SecurityGroupDeleteSpec(@NonNull String id,
                            @NonNull Collection<String> stacks,
                            boolean byCdkId,
                            boolean errorIfNotFound,
                            @Nullable String newName,
                            @Nullable ComponentReference parentReference) {
        super(id, stacks, byCdkId, errorIfNotFound, newName, parentReference);
    }

    /**
     * Builder.
     *
     * @param id      the id
     * @param byCdkId true if the id is a CDK ID, false if a cloud ID
     * @param <D>     the delete spec type
     * @param <B>     the builder type
     * @return a new builder
     */
    @NonNull
    public static <D extends SecurityGroupDeleteSpec<D>, B extends Builder<D, B>> Builder<D, B> builder(
            @NonNull String id, boolean byCdkId) {
        return new Builder<>(id, byCdkId);
    }

    /**
     * Builder.
     *
     * @param <D> the spec type
     * @param <B> the builder type
     */
    public static class Builder<D extends SecurityGroupDeleteSpec<D>, B extends Builder<D, B>> extends DeleteSpec.Builder<D, B> {

        /**
         * Constructor.
         *
         * @param id      ID
         * @param byCdkId {@code true} if id is a CDK ID, false if a cloud ID
         */
        Builder(@NonNull String id, boolean byCdkId) {
            super(id, byCdkId);
        }

        @Override
        @NonNull
        @SuppressWarnings("unchecked")
        protected D doBuild() throws InvalidActionException {
            return (D) new SecurityGroupDeleteSpec<>(
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
