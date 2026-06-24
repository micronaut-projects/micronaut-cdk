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
package io.micronaut.cdk.delta;

import io.micronaut.cdk.action.Action;
import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.component.Component;
import io.micronaut.cdk.util.Assert;
import io.micronaut.core.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Container for actions to be created, actions with existing components to be
 * updated, actions to be deleted, and actions to be executed.
 */
public class Delta {

    private final Collection<Action<?>> create;
    private final Map<DeleteSpec<?>, Component<?>> delete;
    private final Map<Action<?>, Component<?>> update;
    private final Collection<Action<?>> noop;
    private final Collection<Action<?>> execute;
    private final Collection<Action<?>> unspecified;

    /**
     * Constructor.
     *
     * @param create  "create" actions
     * @param delete  "delete" actions
     * @param update  "update" actions
     * @param noop    "no-op" actions (resolved with no updates needed)
     * @param execute "execute" actions
     */
    public Delta(@NonNull Collection<Action<?>> create,
                 @NonNull Map<DeleteSpec<?>, Component<?>> delete,
                 @NonNull Map<Action<?>, Component<?>> update,
                 @NonNull Collection<Action<?>> noop,
                 @NonNull Collection<Action<?>> execute) {
        this(create, delete, update, noop, execute, Collections.emptyList());
    }

    /**
     * Constructor.
     *
     * @param create      "create" actions
     * @param delete      "delete" actions
     * @param update      "update" actions
     * @param noop        "no-op" actions (resolved with no updates needed)
     * @param execute     "execute" actions
     * @param unspecified actions that do not fall into either category
     */
    public Delta(@NonNull Collection<Action<?>> create,
                 @NonNull Map<DeleteSpec<?>, Component<?>> delete,
                 @NonNull Map<Action<?>, Component<?>> update,
                 @NonNull Collection<Action<?>> noop,
                 @NonNull Collection<Action<?>> execute,
                 @NonNull Collection<Action<?>> unspecified) {
        this.create = Collections.unmodifiableCollection(new ArrayList<>(Assert.notNull(create, "create cannot be null")));
        this.delete = Collections.unmodifiableMap(new LinkedHashMap<>(Assert.notNull(delete, "delete cannot be null")));
        this.update = Collections.unmodifiableMap(new LinkedHashMap<>(Assert.notNull(update, "update cannot be null")));
        this.noop = Collections.unmodifiableCollection(new ArrayList<>(Assert.notNull(noop, "noop cannot be null")));
        this.execute = Collections.unmodifiableCollection(new ArrayList<>(Assert.notNull(execute, "execute cannot be null")));
        this.unspecified = Collections.unmodifiableCollection(new ArrayList<>(Assert.notNull(unspecified, "execute cannot be null")));
    }

    /**
     * Actions to use to create new instances.
     *
     * @return actions to use to create new instances
     */
    @NonNull
    public Collection<Action<?>> getCreate() {
        return create;
    }

    /**
     * Actions to delete.
     *
     * @return actions to delete
     */
    @NonNull
    public Map<DeleteSpec<?>, Component<?>> getDelete() {
        return delete;
    }

    /**
     * Actions with associated component to use to update the existing component.
     *
     * @return actions with associated component to use to update the existing component
     */
    @NonNull
    public Map<Action<?>, Component<?>> getUpdate() {
        return update;
    }

    /**
     * Actions to use to create new instances.
     *
     * @return actions to use to create new instances
     */
    @NonNull
    public Collection<Action<?>> getNoop() {
        return noop;
    }

    /**
     * Actions to execute.
     *
     * @return actions to execute
     */
    @NonNull
    public Collection<Action<?>> getExecute() {
        return execute;
    }

    /**
     * Returns unprocessed actions.
     *
     * @return actions other than the matched ones.
     */
    public Collection<Action<?>> getUnspecified() {
        return unspecified;
    }

    @Override
    public String toString() {
        return "Delta{" +
                "create=" + create +
                ", delete=" + delete +
                ", update=" + update +
                ", noop=" + noop +
                ", execute=" + execute +
                '}';
    }
}
