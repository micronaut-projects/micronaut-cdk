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
import io.micronaut.cdk.component.Network;
import io.micronaut.core.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Network spec.
 *
 * @param <S> the spec type
 */
@ResourceSpec(component = Network.class)
public abstract class NetworkSpec<S extends NetworkSpec<S>> extends Spec<S> {

    private List<String> cidrs;
    private String name;

    /**
     * Constructor.
     *
     * @param cdkId  id
     * @param stacks stacks
     */
    protected NetworkSpec(@NonNull String cdkId,
                          @NonNull Collection<String> stacks) {
        super(cdkId, stacks);
    }

    /**
     * CIDR blocks.
     *
     * @return the CIDR blocks
     */
    @NonNull
    public abstract List<String> getCidrBlocks();

    /**
     * First CIDR.
     *
     * @return the first CIDR block
     */
    @NonNull
    public abstract String getCidrBlock();

    /**
     * Name.
     *
     * @return the name
     */
    @NonNull
    public abstract String getName();

    @Override
    public String toString() {
        return super.toString() +
                ", cidrs=" + getCidrBlocks() +
                ", name='" + getName() + '\'';
    }

    /**
     * Builder.
     *
     * @param <S> the spec type
     * @param <B> the builder type
     */
    public abstract static class Builder<S extends NetworkSpec<S>, B extends Builder<S, B>> extends Spec.Builder<S, B> {

        private final List<String> cidrs = new ArrayList<>();
        private String name;

        /**
         * Constructor.
         *
         * @param cdkId CDK ID
         */
        protected Builder(@NonNull String cdkId) {
            super(cdkId);
        }

        /**
         * CIDRs.
         *
         * @param cidrs CIDR blocks
         * @return this
         */
        @NonNull
        public B cidrBlocks(@NonNull List<String> cidrs) {
            this.cidrs.addAll(cidrs);
            return self();
        }

        /**
         * CIDR.
         *
         * @param cidr CIDR block
         * @return this
         */
        @NonNull
        public B cidrBlock(@NonNull String cidr) {
            cidrs.add(cidr);
            return self();
        }

        /**
         * CIDRs.
         *
         * @return the CIDR block
         */
        @NonNull
        public List<String> getCidrBlocks() {
            return cidrs;
        }

        /**
         * The first CIDR.
         *
         * @return the first CIDR block
         */
        @NonNull
        public String getCidrBlock() {
            return cidrs.get(0);
        }

        /**
         * Name.
         *
         * @param name the name
         * @return this
         */
        @NonNull
        public B name(@NonNull String name) {
            this.name = name;
            return self();
        }

        /**
         * Name.
         *
         * @return the name
         */
        @NonNull
        public String getName() {
            return name;
        }

        @Override
        protected void validate() throws InvalidActionException {
            super.validate();

            requireNotNull("cidrs", getCidrBlocks());
            requireState(!getCidrBlocks().isEmpty(), "You must add at least one CIDR block.");
            for (String cidr : getCidrBlocks()) {
                requireHasText("cidr", cidr);
            }

            requireHasText("name", getName());
        }
    }
}
