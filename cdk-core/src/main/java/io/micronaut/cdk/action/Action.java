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
package io.micronaut.cdk.action;

import io.micronaut.cdk.Identified;
import io.micronaut.cdk.component.ComponentReference;
import io.micronaut.cdk.util.Assert;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import static io.micronaut.cdk.action.Stack.DEFAULT_STACK;

/**
 * Base class for actions.
 *
 * @param <A> the action type
 */
public abstract class Action<A extends Action<A>> implements Identified {

    /**
     * logger.
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final String cdkId;
    private final Collection<Action<?>> dependencies = new ArrayList<>();

    private final Collection<String> stacks;

    /**
     * Constructor.
     *
     * @param cdkId  CDK ID
     * @param stacks stacks
     */
    protected Action(@NonNull String cdkId,
                     @NonNull Collection<String> stacks) {
        this.cdkId = Assert.hasText(cdkId, "cdkId is required");
        this.stacks = Assert.notNull(stacks, "stack is required");
    }

    @NonNull
    @Override
    public String getCdkId() {
        return cdkId;
    }

    /**
     * Register a dependency on another action.
     *
     * @param action the action
     */
    public void addDependency(@NonNull Action<?> action) {
        Assert.notNull(action, "action cannot be null");
        Assert.state(!dependencies.contains(action),
                "Dependency on " + action.getCdkId() + " already exists in " + getCdkId());
        dependencies.add(action);
    }

    /**
     * Dependent actions.
     *
     * @return dependent actions
     */
    @NonNull
    public Collection<Action<?>> getDependencies() {
        return Collections.unmodifiableCollection(dependencies);
    }

    /**
     * Stacks.
     *
     * @return stack names
     */
    @NonNull
    public Collection<String> getStacks() {
        return Collections.unmodifiableCollection(stacks);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " +
                "cdkId='" + cdkId + '\'' +
                ", dependencies=" + dependencies.stream().map(Action::getCdkId).toList() +
                ", stacks=" + stacks;
    }

    /**
     * Wrap the string in single quotes if not null.
     *
     * @param s the string
     * @return the quoted string or null
     */
    protected String quote(String s) {
        return s == null ? "null" : '\'' + s + '\'';
    }

    /**
     * Builder.
     *
     * @param <A> the action type
     * @param <B> the builder type
     */
    public abstract static class Builder<A extends Action<A>, B extends Builder<A, B>> {

        private final String cdkId;
        private final Collection<String> stacks = new HashSet<>();

        /**
         * Constructor.
         *
         * @param cdkId CDK ID
         */
        protected Builder(@NonNull String cdkId) {
            this.cdkId = Assert.hasText(cdkId, "cdkId is required");
        }

        /**
         * CDK ID.
         *
         * @return the CDK ID
         */
        @NonNull
        protected String getCdkId() {
            return cdkId;
        }

        /**
         * Stacks.
         *
         * @return stacks
         */
        @NonNull
        public Collection<String> getStacks() {
            return Collections.unmodifiableCollection(stacks);
        }

        /**
         * Defines stacks.
         * The method <b>adds</b> the specified stacks on top of the existing ones.
         *
         * @param stacks stacks
         * @return this
         */
        @NonNull
        public B stacks(@NonNull Collection<String> stacks) {
            this.stacks.addAll(stacks);
            return self();
        }

        /**
         * Clears currently configured stacks.
         * Use this method to clear preconfigured stacks.
         *
         * @return this
         */
        public B clearStacks() {
            this.stacks.clear();
            return self();
        }

        /**
         * Stacks.
         * The method <b>adds</b> the specified stacks on top of the existing ones.
         *
         * @param stacks stacks
         * @return this
         */
        @NonNull
        public B stacks(@NonNull String... stacks) {
            this.stacks(Arrays.asList(stacks));
            return self();
        }

        /**
         * Stacks.
         * The method <b>adds</b> the specified stacks on top of the existing ones.
         *
         * @param stack stack
         * @return this
         */
        @NonNull
        public B stack(@NonNull String stack) {
            this.stacks.add(stack);
            return self();
        }

        /**
         * Return this for method chaining.
         *
         * @return this
         */
        @SuppressWarnings("unchecked")
        @NonNull
        protected B self() {
            return (B) this;
        }

        /**
         * Build.
         *
         * @return the action
         * @throws InvalidActionException if there's a validation or other problem
         */
        @NonNull
        public final A build() throws InvalidActionException {
            validate();
            return registerStacksAndBuild();
        }

        /**
         * Build.
         *
         * @return the action
         * @throws InvalidActionException if there's a validation or other problem
         */
        @NonNull
        protected abstract A doBuild() throws InvalidActionException;

        @SuppressWarnings("unchecked")
        private A registerStacksAndBuild() {
            if (stacks.isEmpty()) {
                stack(DEFAULT_STACK);
            }
            return (A) Stack.registerStacks(doBuild());
        }

        /**
         * Validate that state is valid to build an instance.
         *
         * @throws InvalidActionException if something's not valid
         */
        protected void validate() throws InvalidActionException {
            // override in subclasses
        }

        /**
         * Fail validation if the value of the property is empty.
         *
         * @param property the property name
         * @param value    the value
         * @param <T>      the value type
         * @return the value
         */
        @NonNull
        protected <T extends CharSequence> T requireHasText(@NonNull String property, @Nullable T value) {
            Assert.notNull(property, "property name cannot be null");
            if (!StringUtils.hasText(value)) {
                throw new InvalidActionException(cdkId, property + " is required");
            }
            return value;
        }

        /**
         * Fail validation if the value of the property is shorter than the required minimum length.
         *
         * @param property  the property name
         * @param value     the value
         * @param minLength the minimum required length (>= 0)
         * @param <T>       the value type
         * @return the value
         */
        protected <T extends CharSequence> T requireMinLength(@NonNull String property, T value, int minLength) {
            Assert.notNull(property, "property name cannot be null");
            if (minLength < 0) {
                throw new IllegalArgumentException("minLength must be >= 0");
            }
            if (value == null || value.length() < minLength) {
                throw new InvalidActionException(
                        cdkId,
                        property + " must have at least " + minLength + " characters"
                );
            }
            return value;
        }

        /**
         * Fail validation if the value of the property is null.
         *
         * @param property the property name
         * @param value    the value
         * @param <T>      the value type
         * @return the value
         */
        protected <T> T requireNotNull(@NonNull String property, T value) {
            Assert.notNull(property, "property name cannot be null");
            if (value == null) {
                throw new InvalidActionException(cdkId, property + " is required");
            }
            return value;
        }

        /**
         * Fail validation if the value of the property is not null.
         *
         * @param property the property name
         * @param value    the value
         */
        protected void requireNull(@NonNull String property, Object value) {
            Assert.notNull(property, "property name cannot be null");
            if (value != null) {
                throw new InvalidActionException(cdkId, property + " must be null");
            }
        }

        /**
         * Fail validation if the value is not between min and max inclusive.
         *
         * @param property the property name
         * @param min      the minimum
         * @param max      the maximum
         * @param value    the value
         * @return the value
         */
        protected Number requireBetween(@NonNull String property,
                                        @NonNull Number min,
                                        @NonNull Number max,
                                        @NonNull Number value) {
            requireNotNull("property", property);
            requireNotNull("min", min);
            requireNotNull("max", max);
            requireNotNull(property, value);
            if (value.floatValue() < min.floatValue() || value.floatValue() > max.floatValue()) {
                throw new InvalidActionException(cdkId, property + " (" + value + ") must be between " + min + " and " + max);
            }
            return value;
        }

        /**
         * Check that the reference is not null, and is not "any" or "none".
         *
         * @param propertyName the spec property name
         * @param reference    the reference
         * @return the reference
         */
        @NonNull
        protected ComponentReference requireReference(@NonNull String propertyName,
                                                      @Nullable ComponentReference reference) {
            ComponentReference requiredReference = requireNotNull(propertyName, reference);
            requireState(!requiredReference.isAny(), propertyName + " cannot be 'any'");
            requireState(!requiredReference.isNone(), propertyName + " cannot be 'none'");
            return requiredReference;
        }

        /**
         * Check that the expression is {@code true}.
         *
         * @param expression expression to test
         * @param message    the message
         */
        protected void requireState(boolean expression,
                                    @NonNull String message) {
            if (!expression) {
                throw new InvalidActionException(cdkId, message);
            }
        }

        /**
         * Check that the Secret and its value are not null.
         *
         * @param propertyName the spec property name
         * @param secret       the secret
         */
        protected void requireSecretValue(@NonNull String propertyName,
                                          @NonNull Secret<?> secret) {
            requireNotNull(propertyName, secret);

            Object value = secret.getValue();

            if (value instanceof CharSequence c) {
                requireHasText(propertyName + " value", c);
            } else {
                requireNotNull(propertyName + " value", value);
            }
        }
    }
}
