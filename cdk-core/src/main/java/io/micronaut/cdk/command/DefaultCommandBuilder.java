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

import io.micronaut.cdk.ExecutionContext;
import io.micronaut.cdk.action.Action;
import io.micronaut.cdk.action.Executable;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.component.Component;
import io.micronaut.cdk.delta.Delta;
import io.micronaut.cdk.util.Assert;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Default implementation of {@link CommandBuilder}.
 */
@Singleton
public class DefaultCommandBuilder implements CommandBuilder {

    private final ExecutionContext ec;

    DefaultCommandBuilder(ExecutionContext ec) {
        this.ec = ec;
    }

    @Override
    public void build() throws CommandBuildException {
        Assert.notNull(ec, "ExecutionContext cannot be null");

        Delta delta = ec.getDelta();

        Collection<Command> commands = new ArrayList<>();

        for (Action<?> action : delta.getCreate()) {
            commands.add(Command.create((Spec<?>) action));
        }

        for (Map.Entry<DeleteSpec<?>, Component<?>> entry : delta.getDelete().entrySet()) {
            commands.add(Command.delete(entry.getKey(), entry.getValue()));
        }

        for (Map.Entry<Action<?>, Component<?>> entry : delta.getUpdate().entrySet()) {
            commands.add(Command.update((Spec<?>) entry.getKey(), entry.getValue()));
        }

        for (Action<?> action : delta.getNoop()) {
            commands.add(Command.noop((Spec<?>) action));
        }

        for (Action<?> action : delta.getExecute()) {
            commands.add(Command.execute((Executable<?>) action));
        }

        for (Action<?> action : delta.getUnspecified()) {
            commands.add(Command.noop(action));
        }

        ec.setCommands(new Commands(commands));
    }
}
