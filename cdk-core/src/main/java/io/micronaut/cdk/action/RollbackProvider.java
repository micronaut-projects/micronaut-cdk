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

import io.micronaut.cdk.command.Command;
import io.micronaut.cdk.command.ActionService;
import io.micronaut.cdk.component.Component;
import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ComponentService;
import io.micronaut.core.annotation.Nullable;

import java.util.List;

/**
 * Service responsible for creating actions for completed or failed {@link Command}s. The service
 * must be registered with {@link ComponentService} for a particular {@link Component}
 * or subtypes and optionally with {@link CloudSpecific} to act only for certain cloud.
 * It can be also annotated with {@link ActionService} to restrict to some action subclasses.
 *
 * @param <N> cloud resource type
 * @param <C> component type
 * @param <A> action type
 */
public interface RollbackProvider<N, C extends Component<N>, A extends Action<A>> {
    /**
     * Creates a rollback action for a command. The {@code command} was executed
     * and completed potentially creating, updating, or deleting a {@link Component},
     * or producing a result. The result may not be available for executable commands that were
     * executed by past runs.
     *
     * @param command the command that was executed
     * @param action  the action that should be rolled back (convenience; the same as reported by the command)
     * @param result  the command result. {@code null}, if the command was not completed.
     * @return rollback action that should reverse the {@code command}'s effects.
     */
    List<Action<?>> createRollback(Command command, A action, CommandResult<N, C> result);

    /**
     * Holds command result.
     *
     * @param component  the created or updated component.
     * @param execResult result of command execution.
     * @param error      command execution error
     * @param <N>        cloud resource type
     * @param <C>        component type
     */
    record CommandResult<N, C extends Component<N>>(@Nullable C component,
                                                    @Nullable List<Executable.Result> execResult,
                                                    @Nullable Throwable error) {
    }
}
