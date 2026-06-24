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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Container for {@link Action}s.
 */
public class Actions implements Iterable<Action<?>> {

    private final Collection<? extends Action<?>> actions;

    /**
     * Constructor.
     *
     * @param actions actions
     */
    public Actions(@NonNull Collection<? extends Action<?>> actions) {
        Deque<Action<?>> actionStack = new ArrayDeque<>(Assert.notNull(actions, "actions cannot be null"));
        Collection<Action<?>> aa = new ArrayList<>(actionStack.size());
        Map<Action<?>, Action<?>> map = new IdentityHashMap<>();

        for (Action<?> action : actions) {
            map.put(action, action);
        }

        Action<?> a;
        while ((a = actionStack.poll()) != null) {
            aa.add(a);
            if (a instanceof GroupAction<?> g) {
                for (Action<?> child : g.getContents()) {
                    // If the action is already contained in the supplied actions, do not unroll it.
                    if (!map.containsKey(child)) {
                        actionStack.add(child);
                    }
                }
            }
        }
        this.actions = new ArrayList<>(aa);
    }

    /**
     * Constructor.
     *
     * @param actions actions
     */
    public Actions(@NonNull Action<?>... actions) {
        this(Arrays.asList(Assert.notNull(actions, "actions cannot be null")));
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

    @NonNull
    @Override
    public Iterator<Action<?>> iterator() {
        return getActions().iterator();
    }

    /**
     * Stream the actions.
     *
     * @return the actions as a stream
     */
    @NonNull
    public Stream<? extends Action<?>> stream() {
        return actions.stream();
    }

    @Override
    public String toString() {
        return "Actions{actions=" + actions + '}';
    }
}
