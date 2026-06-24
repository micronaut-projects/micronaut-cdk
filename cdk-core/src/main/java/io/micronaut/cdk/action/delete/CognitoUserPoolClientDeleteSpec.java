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
public class CognitoUserPoolClientDeleteSpec<D extends CognitoUserPoolClientDeleteSpec<D>> extends DeleteSpec<D> {

    private final String clientName;

    /**
     * @param id                id
     * @param stacks            stacks
     * @param errorIfNotFound   if {@code true} throw an exception if the specified
     *                          instance doesn't exist
     * @param newName           optional new name
     * @param userPoolReference user pool reference
     * @param clientName        client name
     */
    CognitoUserPoolClientDeleteSpec(@NonNull String id,
                                    @NonNull Collection<String> stacks,
                                    boolean errorIfNotFound,
                                    @Nullable String newName,
                                    @NonNull ComponentReference userPoolReference,
                                    @NonNull String clientName) {
        super(id, stacks, false, errorIfNotFound, newName, userPoolReference);
        this.clientName = clientName;
    }

    /**
     * User pool reference.
     *
     * @return the reference
     */
    @NonNull
    public ComponentReference getUserPoolReference() {
        return Objects.requireNonNull(getParentReference());
    }

    /**
     * Client name.
     *
     * @return the name
     */
    @NonNull
    public String getClientName() {
        return clientName;
    }

    /**
     * Builder.
     *
     * @param id                the id
     * @param byCdkId           true if the id is a CDK ID, false if a cloud ID
     * @param userPoolReference user pool reference
     * @param <D>               the delete spec type
     * @param <B>               the builder type
     * @return a new builder
     */
    @NonNull
    public static <D extends CognitoUserPoolClientDeleteSpec<D>, B extends Builder<D, B>> Builder<D, B> builder(
            @NonNull String id,
            boolean byCdkId,
            @NonNull ComponentReference userPoolReference) {
        return new Builder<>(id, byCdkId, userPoolReference);
    }

    /**
     * Builder.
     *
     * @param <D> the spec type
     * @param <B> the builder type
     */
    public static class Builder<D extends CognitoUserPoolClientDeleteSpec<D>, B extends Builder<D, B>> extends DeleteSpec.Builder<D, B> {

        @Nullable
        private String clientName;

        /**
         * Constructor.
         *
         * @param id                id
         * @param byCdkId           {@code true} if id is a CDK ID, false if a cloud ID
         * @param userPoolReference user pool reference
         */
        protected Builder(@NonNull String id,
                          boolean byCdkId,
                          @NonNull ComponentReference userPoolReference) {
            super(id, byCdkId);
            parentReference(userPoolReference);
        }

        /**
         * Client name.
         *
         * @param clientName the name
         * @return this
         */
        @NonNull
        public B clientName(String clientName) {
            this.clientName = clientName;
            return self();
        }

        /**
         * Client name.
         *
         * @return the name
         */
        @Nullable
        public String getClientName() {
            return clientName;
        }

        @Override
        @NonNull
        @SuppressWarnings("unchecked")
        protected D doBuild() throws InvalidActionException {
            return (D) new CognitoUserPoolClientDeleteSpec<>(
                    getId(),
                    getStacks(),
                    isErrorIfNotFound(),
                    getNewName(),
                    requireReference("userPoolReference", getParentReference()),
                    requireHasText("clientName", getClientName())
            );
        }

        @Override
        protected void validate() throws InvalidActionException {
            super.validate();

            requireHasText("clientName", getClientName());

            requireReference("userPoolReference", getParentReference());
        }
    }
}
