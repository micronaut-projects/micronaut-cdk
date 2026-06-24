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
package io.micronaut.cdk.impl.action;

import io.micronaut.cdk.ExecutionContext;
import io.micronaut.cdk.action.Action;
import io.micronaut.cdk.action.Actions;
import io.micronaut.cdk.action.Executable;
import io.micronaut.cdk.action.ResourceSpecConverter;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.command.Command;
import io.micronaut.cdk.util.Assert;
import io.micronaut.context.annotation.Prototype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Given a list of actions, it will create an input for {@link RollbackActionsBuilder} to make a delete job
 * out of series of Specs.
 */
@Prototype
public class DeleteJobBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(DeleteJobBuilder.class);
    private final RollbackActionsBuilder rollbackBuilderFactory;
    private final ExecutionContext context;
    private final ResourceSpecConverter converter;

    private List<Action<?>> actions;

    DeleteJobBuilder(RollbackActionsBuilder rollbackBuilderFactory, ExecutionContext context, ResourceSpecConverter converter) {
        this.rollbackBuilderFactory = rollbackBuilderFactory;
        this.context = context;
        this.converter = converter;
    }

    private void setActions(Collection<Action<?>> actions) {
        this.actions = new ArrayList<>(Assert.notNull(actions, "Actions must not be null"));
    }

    /**
     * Computes delete actions for the passed user actions.
     *
     * @param actions user specified actions
     * @return delete actions that delete resources specified by user actions.
     */
    public Actions makeDeleteActions(Collection<Action<?>> actions) {
        setActions(actions);
        List<Command> commands = new ArrayList<>();
        for (Action<?> action : actions) {
            Command cmd = null;

            if (action instanceof DeleteSpec) {
                LOG.info("Ignoring delete action {}", action);
                continue;
            } else if (action instanceof Executable<?> exec) {
                cmd = Command.execute(exec);
                cmd.setCommandStatus(Command.Status.SUCCESSFUL);
                context.registerExecuted(cmd, List.of());
            } else if (action instanceof Spec<?> spec) {
                cmd = Command.create(spec);
                cmd.setCommandStatus(Command.Status.SUCCESSFUL);
            }
            if (cmd != null) {
                commands.add(cmd);
            }
        }
        return new Actions(rollbackBuilderFactory.buildRollback(commands));
    }
}
