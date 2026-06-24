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
public class DkimDeleteSpec<D extends DkimDeleteSpec<D>> extends DeleteSpec<D> {

    /**
     * @param id                   id
     * @param stacks               stacks
     * @param byCdkId              {@code true} if id is a CDK ID, false if a cloud ID
     * @param errorIfNotFound      if {@code true} throw an exception if the specified
     *                             instance doesn't exist
     * @param newName              optional new name
     * @param emailDomainReference email domain reference
     */
    DkimDeleteSpec(@NonNull String id,
                   @NonNull Collection<String> stacks,
                   boolean byCdkId,
                   boolean errorIfNotFound,
                   @Nullable String newName,
                   @NonNull ComponentReference emailDomainReference) {
        super(id, stacks, byCdkId, errorIfNotFound, newName, Objects.requireNonNull(emailDomainReference));
    }

    /**
     * Email domain reference.
     *
     * @return the reference
     */
    @NonNull
    public ComponentReference getEmailDomainReference() {
        return Objects.requireNonNull(getParentReference());
    }

    /**
     * Builder.
     *
     * @param id   the id
     * @param <D>  the delete spec type
     * @param <B>  the builder type
     * @return a new builder
     */
    @NonNull
    public static <D extends DkimDeleteSpec<D>, B extends Builder<D, B>> Builder<D, B> builder(
            @NonNull String id) {
        return builder(id, true);
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
    public static <D extends DkimDeleteSpec<D>, B extends Builder<D, B>> Builder<D, B> builder(
            @NonNull String id,
            boolean byCdkId) {
        return new Builder<>(id, byCdkId);
    }

    /**
     * Builder.
     *
     * @param id                   the id
     * @param byCdkId              true if the id is a CDK ID, false if a cloud ID
     * @param emailDomainReference the email domain reference
     * @param <D>                  the delete spec type
     * @param <B>                  the builder type
     * @return a new builder
     */
    @NonNull
    public static <D extends DkimDeleteSpec<D>, B extends Builder<D, B>> Builder<D, B> builder(
            @NonNull String id,
            boolean byCdkId,
            @NonNull ComponentReference emailDomainReference) {
        return new Builder<>(id, byCdkId, emailDomainReference);
    }

    /**
     * Builder.
     *
     * @param <D> the spec type
     * @param <B> the builder type
     */
    public static class Builder<D extends DkimDeleteSpec<D>, B extends Builder<D, B>> extends DeleteSpec.Builder<D, B> {

        /**
         * Constructor.
         *
         * @param id                   ID
         * @param byCdkId              {@code true} if id is a CDK ID, false if a cloud ID
         * @param emailDomainReference email domain reference
         */
        protected Builder(@NonNull String id,
                          boolean byCdkId,
                          @NonNull ComponentReference emailDomainReference) {
            super(id, byCdkId);
            parentReference(emailDomainReference);
        }

        /**
         * Constructor.
         *
         * @param id      ID
         * @param byCdkId {@code true} if id is a CDK ID, false if a cloud ID
         */
        protected Builder(@NonNull String id,
                          boolean byCdkId) {
            super(id, byCdkId);
        }

        /**
         * Email domain reference.
         *
         * @param emailDomainReference the reference
         * @return this
         */
        @NonNull
        public B emailDomainReference(@NonNull ComponentReference emailDomainReference) {
            parentReference(emailDomainReference);
            return self();
        }

        @Override
        @NonNull
        @SuppressWarnings("unchecked")
        protected D doBuild() throws InvalidActionException {
            return (D) new DkimDeleteSpec<>(
                    getId(),
                    getStacks(),
                    isByCdkId(),
                    isErrorIfNotFound(),
                    getNewName(),
                    requireReference("emailDomainReference", getParentReference())
            );
        }

        @Override
        protected void validate() throws InvalidActionException {
            super.validate();

            requireReference("emailDomainReference", getParentReference());
        }
    }
}
