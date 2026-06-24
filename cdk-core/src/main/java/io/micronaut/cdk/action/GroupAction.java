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

import io.micronaut.core.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A special configurable Action that only serves as a container for zero or more other
 * actions. This action itself does not represent any action taken on the cloud, just groups
 * other actions together. The GroupAction can be also used as a placeholder to anchor
 * dependencies. The GroupAction can act as a <b>unit</b>: actions it contains start only after
 * predecessors for all actions complete, and successive actions may start only after all grouped
 * actions complete.
 *
 * @param <T> action own type.
 */
public final class GroupAction<T extends GroupAction<T>> extends Action<T> implements Iterable<Action<?>> {
    private final String label;
    private final List<Action<?>> contents;
    private final boolean ordered;
    private final boolean internal;
    private final boolean unit;

    GroupAction(String cdkId, Collection<String> stacks, String label, List<Action<?>> contents, boolean ordered, boolean internal, boolean unit) {
        super(cdkId, stacks);
        this.label = label;
        this.ordered = ordered;
        this.contents = contents;
        this.internal = internal;
        this.unit = unit;
    }

    public List<Action<?>> getContents() {
        return contents;
    }

    public boolean isOrdered() {
        return ordered;
    }

    @Override
    @NonNull
    public Iterator<Action<?>> iterator() {
        return contents.iterator();
    }

    /**
     * Returns a descriptive label for the group. The default label is derived
     * from the CDK ID.
     *
     * @return label string.
     */
    public String getLabel() {
        return label;
    }

    /**
     * True, if the group is an internal artifact and should not be presented
     * to the users.
     *
     * @return true for internally generated groups.
     */
    public boolean isInternal() {
        return internal;
    }

    /**
     * True, if the group is a unit.
     *
     * @return true for unit groups.
     */
    public boolean isUnit() {
        return unit;
    }

    public static <A extends GroupAction<A>, B extends Builder<A, B>> Builder<A, B> builder(String cdkId) {
        return new Builder<>(cdkId);
    }

    /**
     * Builder for {@link GroupAction}.
     *
     * @param <A> action type
     * @param <B> builder type
     */
    public static final class Builder<A extends GroupAction<A>, B extends Builder<A, B>> extends Action.Builder<A, B> {
        private boolean ordered;
        private String label;
        private boolean internal;
        private boolean unit;
        private final List<Action<?>> contents = new ArrayList<>();

        public Builder(String cdkId) {
            super(cdkId);
            label = "(" + cdkId + ")";
        }

        /**
         * Adds an action to the group.
         *
         * @param action action to add
         * @return this instance.
         */
        public B add(Action<?>... action) {
            if (action == null) {
                return self();
            }
            this.contents.addAll(Arrays.asList(action));
            return self();
        }

        /**
         * True, if the group will act as a unit.
         *
         * @return true for unit group.
         */
        public boolean isUnit() {
            return unit;
        }

        /**
         * Sets the unit flag.
         *
         * @param unit flag value
         * @return this instance
         */
        public B setUnit(boolean unit) {
            this.unit = unit;
            return self();
        }

        /**
         * Returns the value of 'ordered' property.
         *
         * @return True, if the group should order the execution
         */
        public boolean isOrdered() {
            return ordered;
        }

        /**
         * True, if the group will be an internal one.
         *
         * @return true for internal groups.
         */
        public boolean isInternal() {
            return internal;
        }

        /**
         * Returns current group's contents.
         *
         * @return actions in the group.
         */
        public List<Action<?>> getContents() {
            return contents;
        }

        /**
         * Marks the group as ordered. Actions should execute in the order they are in the group.
         *
         * @param ordered true, if the group should enforce the action's order
         * @return this instance.
         */
        public B ordered(boolean ordered) {
            this.ordered = ordered;
            return self();
        }

        /**
         * Assigns a label to the group. The label is a descriptive string with no
         * semantic meaning. The default label is derived from CDK ID.
         *
         * @param label descriptive string
         * @return this instance.
         */
        public B label(String label) {
            this.label = label;
            return self();
        }

        /**
         * Marks the GroupAction as internal. Internal actions may be filtered out of
         * the user reports, but can serve as anchors for dependencies during the process.
         *
         * @param internal true to mark the action as internal.
         * @return this instance.
         */
        public B internal(boolean internal) {
            this.internal = internal;
            return self();
        }

        @SuppressWarnings("unchecked")
        @Override
        protected A doBuild() throws InvalidActionException {
            return (A) new GroupAction<>(getCdkId(), getStacks(), label, contents, ordered, internal, unit);
        }
    }
}
