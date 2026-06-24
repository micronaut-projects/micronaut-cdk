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
import java.util.regex.Pattern;

/**
 * Un-deploy by CDK ID or cloud ID.
 *
 * @param <D> the delete spec type
 */
public class CognitoUserPoolDomainDeleteSpec<D extends CognitoUserPoolDomainDeleteSpec<D>> extends DeleteSpec<D> {

    private final String domainName;

    /**
     * @param id                id
     * @param stacks            stacks
     * @param errorIfNotFound   if {@code true} throw an exception if the specified
     *                          instance doesn't exist
     * @param newName           optional new name
     * @param userPoolReference user pool reference
     * @param domainName        domain name
     */
    CognitoUserPoolDomainDeleteSpec(@NonNull String id,
                                    @NonNull Collection<String> stacks,
                                    boolean errorIfNotFound,
                                    @Nullable String newName,
                                    @NonNull ComponentReference userPoolReference,
                                    @NonNull String domainName) {
        super(id, stacks, false, errorIfNotFound, newName, userPoolReference);
        this.domainName = domainName;
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
     * Domain name.
     *
     * @return the name
     */
    @NonNull
    public String getDomainName() {
        return domainName;
    }

    /**
     * Builder.
     *
     * @param cdkId             CDK ID
     * @param userPoolReference user pool reference
     * @param <D>               the delete spec type
     * @param <B>               the builder type
     * @return a new builder
     */
    @NonNull
    public static <D extends CognitoUserPoolDomainDeleteSpec<D>, B extends Builder<D, B>> Builder<D, B> builder(
            @NonNull String cdkId,
            @NonNull ComponentReference userPoolReference) {
        return new Builder<>(cdkId, true, userPoolReference);
    }

    /**
     * Builder.
     *
     * @param <D> the spec type
     * @param <B> the builder type
     */
    public static class Builder<D extends CognitoUserPoolDomainDeleteSpec<D>, B extends Builder<D, B>> extends DeleteSpec.Builder<D, B> {

        private static final Pattern DOMAIN_NAME_REGEX = Pattern.compile("^[a-z0-9](?:[a-z0-9\\-]{0,61}[a-z0-9])?$");

        @Nullable
        private String domainName;

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
         * Domain name.
         *
         * @param domainName the name
         * @return this
         */
        @NonNull
        public B domainName(@NonNull String domainName) {
            this.domainName = domainName;
            return self();
        }

        /**
         * Domain name.
         *
         * @return the name
         */
        @Nullable
        public String getDomainName() {
            return domainName;
        }

        @Override
        @NonNull
        @SuppressWarnings("unchecked")
        protected D doBuild() throws InvalidActionException {
            return (D) new CognitoUserPoolDomainDeleteSpec<>(
                    getId(),
                    getStacks(),
                    isErrorIfNotFound(),
                    getNewName(),
                    requireReference("userPool", getParentReference()),
                    requireHasText("domainName", getDomainName())
            );
        }

        @Override
        protected void validate() throws InvalidActionException {
            super.validate();

            requireHasText("domainName", getDomainName());

            requireState(DOMAIN_NAME_REGEX.matcher(getDomainName()).matches(),
                    "Domain names can only contain lower-case letters, numbers, and hyphens");

            requireReference("userPool", getParentReference());
        }
    }
}
