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

import io.micronaut.cdk.util.Assert;
import io.micronaut.core.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains zero or more actions, supporting partitioning a larger group of
 * actions to deploy a subset of all existing actions.
 */
public class Stack {

    /**
     * Default stack name.
     */
    public static final String DEFAULT_STACK = "default__stack__";

    private static final Map<String, Stack> STACKS = new HashMap<>();

    private final String name;
    private final Collection<Action<?>> actions = new ArrayList<>();

    static {
        reset();
    }

    /**
     * @param name    the stack name
     * @param actions the stack actions
     */
    Stack(@NonNull String name,
          @NonNull Collection<? extends Action<?>> actions) {
        this.name = name;
        this.actions.addAll(actions);
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

    /**
     * Actions.
     *
     * @return the actions
     */
    @NonNull
    public Collection<Action<?>> getActions() {
        return Collections.unmodifiableCollection(actions);
    }

    /**
     * Add action to the stack.
     *
     * @param action action
     */
    public void addAction(@NonNull Action<?> action) {
        actions.add(action);
    }

    /**
     * Add actions to the stack.
     *
     * @param actions actions
     */
    public void addActions(@NonNull Collection<? extends Action<?>> actions) {
        this.actions.addAll(actions);
    }

    /**
     * Add actions to the stack.
     *
     * @param actions actions
     */
    public void addActions(@NonNull Action<?>... actions) {
        addActions(Arrays.asList(actions));
    }

    /**
     * Checks if the stack exists.
     *
     * @param stackName the stack
     * @return true if the stack exists
     */
    @NonNull
    public static boolean exists(@NonNull String stackName) {
        return STACKS.get(stackName) != null;
    }

    /**
     * Get a stack by name.
     *
     * @param name stack name
     * @return the stack, or null if none exists with the specified name
     */
    @NonNull
    public static Stack get(@NonNull String name) {
        Assert.notNull(name, "Stack name cannot be null");

        if (!exists(name)) {
            throw new IllegalStateException("stack '" + name + "' Not Found!");
        }
        return STACKS.get(name);
    }

    /**
     * Stacks.
     *
     * @return all registered stacks
     */
    @NonNull
    public static Collection<Stack> getStacks() {
        return Collections.unmodifiableCollection(STACKS.values());
    }

    /**
     * Register a stack.
     *
     * @param stack a stack
     * @return the stack
     */
    @NonNull
    public static Stack register(@NonNull Stack stack) {
        return STACKS.put(stack.getName(), stack);
    }

    /**
     * Register stacks for the specified action.
     *
     * @param action the action
     * @param <A>    the action type
     * @return the action
     */
    @NonNull
    public static <A extends Action<A>> Action<A> registerStacks(@NonNull A action) {
        Assert.notNull(action, "Action cannot be null");

        for (String stack : action.getStacks()) {
            if (STACKS.containsKey(stack)) {
                get(stack).addAction(action);
            } else {
                register(Stack.builder(stack)
                        .action(action)
                        .build());
            }
        }

        return action;
    }

    /**
     * For tests.
     */
    static void reset() {
        STACKS.clear();
        STACKS.put(DEFAULT_STACK, Stack.builder(DEFAULT_STACK).build());
    }

    /**
     * Builder.
     *
     * @param stackName the stack name
     * @return a new builder
     */
    public static Builder builder(@NonNull String stackName) {
        return new Builder(stackName);
    }

    @Override
    public String toString() {
        return "name='" + name + '\'' +
                ", actions=" + actions;
    }

    /**
     * Builder.
     */
    public static class Builder {

        private final String name;

        private final Collection<Action<?>> actions = new ArrayList<>();

        /**
         * @param name stack name
         */
        Builder(@NonNull String name) {
            this.name = name;
        }

        /**
         * Stack name.
         *
         * @return the name
         */
        @NonNull
        public String getName() {
            return name;
        }

        /**
         * add action.
         *
         * @param action action
         * @return this
         */
        @NonNull
        public Builder action(@NonNull Action<?> action) {
            this.actions.add(action);
            return this;
        }

        /**
         * Add actions.
         *
         * @param actions actions
         * @return this
         */
        @NonNull
        public Builder actions(@NonNull Collection<? extends Action<?>> actions) {
            this.actions.addAll(actions);
            return this;
        }

        /**
         * Add actions.
         *
         * @param actions actions
         * @return this
         */
        @NonNull
        public Builder actions(@NonNull Action<?>... actions) {
            this.actions(Arrays.asList(actions));
            return this;
        }

        /**
         * Actions.
         *
         * @return the actions
         */
        @NonNull
        public Collection<Action<?>> getActions() {
            return Collections.unmodifiableCollection(actions);
        }

        /**
         * Build.
         *
         * @return the stack
         */
        @NonNull
        public Stack build() {
            return new Stack(
                    getName(),
                    getActions()
            );
        }
    }
}
