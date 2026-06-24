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
import io.micronaut.cdk.action.Executable;
import io.micronaut.cdk.action.GroupAction;
import io.micronaut.cdk.action.ResourceSpecConverter;
import io.micronaut.cdk.action.RollbackProvider;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.annotations.ComponentService;
import io.micronaut.cdk.command.ActionService;
import io.micronaut.cdk.command.Command;
import io.micronaut.cdk.command.Commands;
import io.micronaut.cdk.component.Component;
import io.micronaut.cdk.graph.Graph;
import io.micronaut.cdk.graph.GraphBuilder;
import io.micronaut.cdk.graph.Node;
import io.micronaut.cdk.impl.CdkQualifiers;
import io.micronaut.cdk.util.Assert;
import io.micronaut.context.BeanContext;
import io.micronaut.context.Qualifier;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.inject.qualifiers.Qualifiers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper bean that builds actions for rolling back Command changes. The bean is always injected
 * as fresh instance; use {@link jakarta.inject.Provider<RollbackActionsBuilder>} to generate multiple
 * instances.
 */
@Prototype
public class RollbackActionsBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(RollbackActionsBuilder.class);

    private final ExecutionContext executionContext;
    private final BeanContext beanContext;
    private final ResourceSpecConverter converter;

    private final List<Action<?>> rollbacks = new ArrayList<>();
    private final Map<Command, List<Action<?>>> commandRollbacks = new HashMap<>();

    private final Map<Action<?>, Command> action2Commands = new HashMap<>();
    private List<Command> commands;

    public RollbackActionsBuilder(ExecutionContext executionContext, BeanContext beanContext, ResourceSpecConverter converter) {
        this.executionContext = executionContext;
        this.beanContext = beanContext;
        this.converter = converter;
    }

    private void setCommands(List<Command> commands) {
        this.commands = Assert.notNull(commands, "Commands cannot be null");
    }

    private void indexActions() {
        for (Command cmd : commands) {
            if (action2Commands.putIfAbsent(cmd.getAction(), cmd) != null) {
                throw new IllegalStateException("Duplicate action " + cmd.getAction());
            }
        }
    }

    /**
     * Builds a list of rollback actions for the commands. Taking commands set by {@link #setCommands(List)}, produces a
     * list of actions to roll back those actions.
     *
     * @param commands commands to be rolled back.
     * @return list of rollback actions.
     */
    public List<Action<?>> buildRollback(List<Command> commands) {
        setCommands(commands);
        indexActions();
        createRollbacks();
        reverseActionDependencies();
        return rollbacks;
    }

    /**
     * Generates a list of action to rollback a resource action.
     *
     * @param cmd command working with a resource
     * @return list of rollback actions.
     */
    @SuppressWarnings("rawtypes")
    private List<Action<?>> resourceRollback(Command cmd) {
        Action<?> a = cmd.getAction();
        Component<?> comp = executionContext.getActionsAndComponents().get(cmd.getAction());
        Class<? extends Component> componentClass = null;
        if (comp == null) {
            if (a instanceof Spec<?> spec) {
                if (!cmd.isCreate()) {
                    return List.of();
                }
                componentClass = converter.findComponentClass(spec);
            } else if (a instanceof DeleteSpec<?> deleteSpec) {
                componentClass = deleteSpec.getComponentClass();
                if (componentClass == null) {
                    componentClass = converter.findComponentClass(deleteSpec);
                }
            }
            if (componentClass != null) {
                if (a instanceof Spec<?> spec) {
                    comp = executionContext.findDeployedComponent(spec.getCdkId(), true, componentClass).orElse(null);
                } else if (a instanceof DeleteSpec<?> deleteSpec) {
                    comp = executionContext.findDeployedComponent(deleteSpec.getId(), deleteSpec.isByCdkId(), componentClass).orElse(null);
                } else {
                    throw new UnsupportedOperationException("Unsupported action: " + cmd.getAction());
                }
            } else {
                throw new IllegalStateException("Cannot find component type for " + cmd.getAction());
            }
        } else {
            componentClass = comp.getClass();
        }
        // TODO each Command or Action should know which Cloud it operates on. Use it as Qualifier then.
        Qualifier<RollbackProvider> qual = Qualifiers.byQualifiers(
                CdkQualifiers.preferAcceptsType(ActionService.class, cmd.getAction().getClass()),
                CdkQualifiers.acceptsType(ComponentService.class, componentClass)
        );

        Collection<RollbackProvider> rollbackProviders = beanContext.getBeansOfType(RollbackProvider.class, qual);
        for (RollbackProvider rollbackProvider : rollbackProviders) {
            RollbackProvider.CommandResult<?, ?> result;
            if (cmd.getCommandStatus() == Command.Status.SUCCESSFUL) {
                if (cmd.isExecutable()) {
                    List<Executable.Result> execResults = executionContext.getAllExecuted().get(cmd);
                    result = new RollbackProvider.CommandResult<>(null,
                            execResults, null);
                } else {
                    result = new RollbackProvider.CommandResult<>(comp, null, null);
                }
            } else {
                result = new RollbackProvider.CommandResult<>(null, null, cmd.getFailureCause());
            }
            @SuppressWarnings("unchecked")
            List<Action<?>> rollbackActions = rollbackProvider.createRollback(cmd, cmd.getAction(), result);
            if (rollbackActions != null) {
                return rollbackActions;
            }
        }
        return null;
    }

    private void reverseActionDependencies() {
        GraphBuilder builder = new GraphBuilder(new Commands(this.commands));
        Graph originalGraph = builder.build();
        for (Node n : originalGraph.getNodes()) {
            Command predCommand = n.getCommand();
            if (!commands.contains(predCommand)) {
                throw new IllegalStateException("Predecessor " + predCommand + " has no rollback");
            }
            LOG.debug("Checking successors of " + predCommand);
            List<Action<?>> predActions = commandRollbacks.get(predCommand);
            List<Action<?>> dependencies = new ArrayList<>();
            for (Node succ : n.getSuccessors()) {
                Command succCommand = succ.getCommand();
                if (!commands.contains(succCommand)) {
                    LOG.debug("Successor {}  is not part of the rollback", succCommand);
                    continue;
                }
                List<Action<?>> succActions = commandRollbacks.get(succCommand);
                if (succActions == null) {
                    LOG.warn("Command {} has no rollback registered", succCommand);
                    continue;
                }
                LOG.debug("Original successor {} has rollback actions: {}", succCommand, succActions);
                dependencies.addAll(succActions);
            }
            LOG.debug("Adding predecessors to command {}, actions {}: {}", predCommand, predActions, dependencies);
            for (Action<?> action : predActions) {
                for (Action<?> dependency : dependencies) {
                    action.addDependency(dependency);
                }
            }
        }
    }

    private void createRollbacks() {
        for (Command cmd : commands) {
            List<Action<?>> rb;

            switch (cmd.getCommandStatus()) {
                case SKIPPED:
                    continue;
                case SUCCESSFUL:
                case FAILED:
                    if (cmd.isCreate() || cmd.isUpdate() || cmd.isDelete()) {
                        rb = resourceRollback(cmd);
                        if (rb != null) {
                            this.commandRollbacks.put(cmd, rb);
                            this.rollbacks.addAll(rb);
                            break;
                        }
                    } else if (cmd.isNoOp()) {
                        this.commandRollbacks.put(cmd,
                                List.of(
                                        // retain an action for possible dependencies.
                                        GroupAction.builder(cmd.getAction().getCdkId())
                                                .internal(true)
                                                .label("Placeholder for no-op action")
                                                .build()
                                )
                        );
                        break;
                    }
                    throw new IllegalStateException("Command " + cmd + " cannot be rolled back");

                default:
                    throw new IllegalStateException("Command " + cmd + " is in unexpected state: " + cmd.getCommandStatus().name());
            }
        }
    }

}
