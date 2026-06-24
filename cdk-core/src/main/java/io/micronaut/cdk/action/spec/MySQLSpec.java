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
import io.micronaut.cdk.action.Secret;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.annotations.ResourceSpec;
import io.micronaut.cdk.component.MySQL;
import io.micronaut.core.annotation.NonNull;

import java.util.Collection;

/**
 * MySQL database spec.
 *
 * @param <S> the spec type
 */
@ResourceSpec(component = MySQL.class)
public abstract class MySQLSpec<S extends MySQLSpec<S>> extends Spec<S> {

    /**
     * Constructor.
     *
     * @param cdkId  id
     * @param stacks stacks
     */
    protected MySQLSpec(@NonNull String cdkId,
                        @NonNull Collection<String> stacks) {
        super(cdkId, stacks);
    }

    /**
     * Admin password.
     *
     * @return the admin password
     */
    @NonNull
    public abstract Secret<String> getAdminPassword();

    /**
     * Admin username.
     *
     * @return the admin username
     */
    @NonNull
    public abstract String getAdminUsername();

    /**
     * Display name.
     *
     * @return the display name
     */
    @NonNull
    public abstract String getDisplayName();

    /**
     * DB shape.
     *
     * @return the shape
     */
    @NonNull
    public abstract String getShape();

    @Override
    public String toString() {
        return super.toString() +
                ", adminPassword=" + getAdminPassword() +
                ", adminUsername='" + getAdminUsername() + '\'' +
                ", displayName=" + quote(getDisplayName()) +
                ", shape='" + getShape() + '\'';
    }

    /**
     * Builder.
     *
     * @param <S> the spec type
     * @param <B> the builder type
     */
    public abstract static class Builder<S extends MySQLSpec<S>, B extends Builder<S, B>> extends Spec.Builder<S, B> {

        private Secret<String> adminPassword;
        private String adminUsername;
        private String displayName;
        private String shape;

        /**
         * Constructor.
         *
         * @param cdkId CDK ID
         */
        protected Builder(@NonNull String cdkId) {
            super(cdkId);
        }

        /**
         * Admin password.
         *
         * @param adminPassword the admin password
         * @return this
         */
        @NonNull
        public B adminPassword(@NonNull String adminPassword) {
            this.adminPassword = new Secret<>(adminPassword);
            return self();
        }

        /**
         * Admin password.
         *
         * @return the admin password
         */
        @NonNull
        public Secret<String> getAdminPassword() {
            return adminPassword;
        }

        /**
         * Admin username.
         *
         * @param adminUsername the admin username
         * @return this
         */
        @NonNull
        public B adminUsername(@NonNull String adminUsername) {
            this.adminUsername = adminUsername;
            return self();
        }

        /**
         * Admin username.
         *
         * @return the admin username
         */
        @NonNull
        public String getAdminUsername() {
            return adminUsername;
        }

        /**
         * Display name.
         *
         * @param displayName the display name
         * @return this
         */
        @NonNull
        public B displayName(@NonNull String displayName) {
            this.displayName = displayName;
            return self();
        }

        /**
         * Display name.
         *
         * @return the display name
         */
        @NonNull
        public String getDisplayName() {
            return displayName == null ? getCdkId() : displayName;
        }

        /**
         * DB shape.
         *
         * @param shape the shape
         * @return this
         */
        @NonNull
        public B shape(@NonNull String shape) {
            this.shape = shape;
            return self();
        }

        /**
         * DB shape.
         *
         * @return the shape
         */
        @NonNull
        public String getShape() {
            return shape;
        }

        @Override
        protected void validate() throws InvalidActionException {
            super.validate();

            requireSecretValue("adminPassword", getAdminPassword());

            requireHasText("adminUsername", getAdminUsername());

            requireHasText("shape", getShape());
        }
    }
}
