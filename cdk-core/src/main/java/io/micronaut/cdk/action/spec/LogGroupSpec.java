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
import io.micronaut.cdk.annotations.ResourceSpec;
import io.micronaut.cdk.component.LogGroup;
import io.micronaut.core.annotation.NonNull;

import java.util.Collection;

/**
 * LogGroup spec.
 *
 * @param <S> the spec type
 */
@ResourceSpec(component = LogGroup.class)
public abstract class LogGroupSpec<S extends LogGroupSpec<S>> extends Spec<S> {

    /**
     * Constructor.
     *
     * @param cdkId       id
     * @param stacks      stacks
     */
    protected LogGroupSpec(@NonNull String cdkId,
                           @NonNull Collection<String> stacks) {
        super(cdkId, stacks);
    }

    /**
     * Display name.
     *
     * @return the display name
     */
    @NonNull
    public abstract String getDisplayName();

    @Override
    public String toString() {
        return super.toString() +
                ", logGroupName= " + getDisplayName();
    }

    /**
     * Builder.
     *
     * @param <S> the spec type
     * @param <B> the builder type
     */
    public abstract static class Builder<S extends LogGroupSpec<S>, B extends Builder<S, B>> extends Spec.Builder<S, B> {

        private String displayName;

        /**
         * Constructor.
         *
         * @param cdkId CDK ID
         */
        protected Builder(@NonNull String cdkId) {
            super(cdkId);
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
            return displayName;
        }

        @Override
        protected void validate() throws InvalidActionException {
            super.validate();
        }
    }
}
