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
public class SmtpCredentialDeleteSpec<D extends SmtpCredentialDeleteSpec<D>> extends DeleteSpec<D> {

    private final String description;

    /**
     * @param id              id
     * @param stacks          stacks
     * @param byCdkId         {@code true} if id is a CDK ID, false if a cloud ID
     * @param errorIfNotFound if {@code true} throw an exception if the specified
     *                        instance doesn't exist
     * @param newName         optional new name
     * @param description     description
     * @param userReference   user reference
     */
    SmtpCredentialDeleteSpec(@NonNull String id,
                             @NonNull Collection<String> stacks,
                             boolean byCdkId,
                             boolean errorIfNotFound,
                             @Nullable String newName,
                             @NonNull String description,
                             @NonNull ComponentReference userReference) {
        super(id, stacks, byCdkId, errorIfNotFound, newName, Objects.requireNonNull(userReference));
        this.description = description;
    }

    /**
     * Description.
     *
     * @return the description
     */
    @NonNull
    public String getDescription() {
        return description;
    }

    /**
     * User reference.
     *
     * @return the reference
     */
    @NonNull
    public ComponentReference getUserReference() {
        return Objects.requireNonNull(getParentReference());
    }

    @Override
    public String toString() {
        return super.toString() + ", description='" + description;
    }

    /**
     * Builder.
     *
     * @param id            the id
     * @param byCdkId       true if the id is a CDK ID, false if a cloud ID
     * @param userReference the user reference
     * @param <D>           the delete spec type
     * @param <B>           the builder type
     * @return a new builder
     */
    @NonNull
    public static <D extends SmtpCredentialDeleteSpec<D>, B extends Builder<D, B>> Builder<D, B> builder(
            @NonNull String id,
            boolean byCdkId,
            @NonNull ComponentReference userReference) {
        return new Builder<>(id, byCdkId, userReference);
    }

    /**
     * Builder.
     *
     * @param <D> the spec type
     * @param <B> the builder type
     */
    public static class Builder<D extends SmtpCredentialDeleteSpec<D>, B extends Builder<D, B>> extends DeleteSpec.Builder<D, B> {

        @Nullable
        private String description;

        /**
         * Constructor.
         *
         * @param id            ID
         * @param byCdkId       {@code true} if id is a CDK ID, false if a cloud ID
         * @param userReference user reference
         */
        protected Builder(@NonNull String id,
                          boolean byCdkId,
                          @NonNull ComponentReference userReference) {
            super(id, byCdkId);
            parentReference(userReference);
        }

        /**
         * Description.
         *
         * @param description the description
         * @return this
         */
        @NonNull
        public B description(@NonNull String description) {
            this.description = description;
            return self();
        }

        /**
         * Description.
         *
         * @return the description
         */
        @Nullable
        public String getDescription() {
            return description;
        }

        @Override
        @NonNull
        @SuppressWarnings("unchecked")
        protected D doBuild() throws InvalidActionException {
            return (D) new SmtpCredentialDeleteSpec<>(
                    getId(),
                    getStacks(),
                    isByCdkId(),
                    isErrorIfNotFound(),
                    getNewName(),
                    requireHasText("description", getDescription()),
                    requireReference("userReference", getParentReference())
            );
        }

        @Override
        protected void validate() throws InvalidActionException {
            super.validate();

            requireHasText("description", getDescription());

            requireReference("userReference", getParentReference());
        }
    }
}
