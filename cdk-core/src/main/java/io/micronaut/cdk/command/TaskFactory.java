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
package io.micronaut.cdk.command;

import io.micronaut.core.annotation.NonNull;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Creates tasks.
 *
 * @param <T> the result type
 */
public interface TaskFactory<T> {

    /**
     * Create tasks.
     *
     * @param commands commands
     * @return the tasks
     */
    @NonNull
    default List<Callable<T>> createTasks(@NonNull List<T> commands) {
        return commands.stream()
                .map(this::createTask)
                .toList();
    }

    /**
     * Create single task.
     *
     * @param command the command
     * @return the task
     */
    @NonNull
    Callable<T> createTask(@NonNull T command);
}
