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
import io.micronaut.cdk.component.VirtualMachine;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

import java.util.Collection;

/**
 * VM spec.
 *
 * @param <S> the spec type
 */
@ResourceSpec(component = VirtualMachine.class)
public abstract class VirtualMachineSpec<S extends VirtualMachineSpec<S>> extends Spec<S> {

    /**
     * Constructor.
     *
     * @param cdkId         id
     * @param stacks        stacks
     */
    protected VirtualMachineSpec(@NonNull String cdkId,
                                 @NonNull Collection<String> stacks) {
        super(cdkId, stacks);
    }

    /**
     * Disk image name.
     *
     * @return the disk image name
     */

    @Nullable
    public abstract String getDiskImageName();

    /**
     * Display name.
     *
     * @return the display name
     */
    @NonNull
    public abstract String getDisplayName();

    /**
     * Shape name.
     *
     * @return the shape name
     */
    @Nullable
    public abstract String getShapeName();

    @Override
    public String toString() {
        return super.toString() +
                ", diskImageName=" + quote(getDiskImageName()) +
                ", displayName='" + getDisplayName() + '\'' +
                ", shapeName=" + quote(getShapeName());
    }

    /**
     * Builder.
     *
     * @param <S> the spec type
     * @param <B> the builder type
     */
    public abstract static class Builder<S extends VirtualMachineSpec<S>, B extends Builder<S, B>> extends Spec.Builder<S, B> {

        private String diskImageName;
        private String displayName;
        private String shapeName;

        /**
         * Constructor.
         *
         * @param cdkId CDK ID
         */
        protected Builder(@NonNull String cdkId) {
            super(cdkId);
        }

        /**
         * Disk image name.
         *
         * @param diskImageName the disk image name
         * @return this
         */
        @NonNull
        public B diskImageName(@NonNull String diskImageName) {
            this.diskImageName = diskImageName;
            return self();
        }

        /**
         * Disk image name.
         *
         * @return the disk image name
         */
        @Nullable
        public String getDiskImageName() {
            return diskImageName;
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
         * Shape name.
         *
         * @param shapeName the shape name
         * @return this
         */
        @NonNull
        public B shapeName(@NonNull String shapeName) {
            this.shapeName = shapeName;
            return self();
        }

        /**
         * Shape name.
         *
         * @return the shape name
         */
        @Nullable
        public String getShapeName() {
            return shapeName;
        }

        @Override
        protected void validate() throws InvalidActionException {
            super.validate();

            requireHasText("displayName", getDisplayName());
        }

    }
}
