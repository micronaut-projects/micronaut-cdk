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

import io.micronaut.cdk.CdkProcessOptions;
import io.micronaut.cdk.Cloud;
import io.micronaut.cdk.ExecutionContext;
import io.micronaut.cdk.action.validation.ActionsValidator;
import io.micronaut.cdk.command.Command;
import io.micronaut.cdk.command.CommandBuilder;
import io.micronaut.cdk.command.CommandRunner;
import io.micronaut.cdk.command.CommandsRunResult;
import io.micronaut.cdk.command.RunReport;
import io.micronaut.cdk.component.ExistingComponents;
import io.micronaut.cdk.delta.DeltaResolver;
import io.micronaut.cdk.impl.action.DeleteJobBuilder;
import io.micronaut.cdk.impl.action.RollbackActionsBuilder;
import io.micronaut.cdk.util.StringCleaner;
import io.micronaut.cdk.validation.LimitValidator;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

import static io.micronaut.cdk.FailureMode.LAZY_FAILURE;
import static io.micronaut.cdk.FailureMode.ROLLBACK;

/**
 * Default implementation of {@link ActionProcessor}.
 */
@Singleton
public final class DefaultActionProcessor implements ActionProcessor {

    private final ExecutionContext ec;
    private final ActionsValidator actionsValidator;
    private final CommandBuilder commandBuilder;
    private final CommandRunner commandRunner;
    private final DeltaResolver deltaResolver;
    private final LimitValidator limitValidator;
    private final ExistingComponentResolver existingComponentResolver;
    private final StringCleaner stringCleaner;
    private final ActionDependenciesConfigurer actionDependenciesConfigurer = new ActionDependenciesConfigurer();

    private Cloud defaultCloud;

    private RollbackActionsBuilder rollbackBuilder;
    private DeleteJobBuilder deleteJobBuilder;

    DefaultActionProcessor(ExecutionContext ec,
                           ActionsValidator actionsValidator,
                           CommandBuilder commandBuilder,
                           CommandRunner commandRunner,
                           DeltaResolver deltaResolver,
                           ExistingComponentResolver existingComponentResolver,
                           @Nullable LimitValidator limitValidator,
                           @Nullable StringCleaner stringCleaner) {
        this.ec = ec;
        this.actionsValidator = actionsValidator;
        this.commandBuilder = commandBuilder;
        this.commandRunner = commandRunner;
        this.deltaResolver = deltaResolver;
        this.limitValidator = limitValidator;
        this.existingComponentResolver = existingComponentResolver;
        this.stringCleaner = stringCleaner;
    }

    /**
     * Injects the default cloud. By default, OCI is pre-selected.
     *
     * @param defaultCloud default cloud setting.
     */
    @Inject
    public void setDefaultCloud(@Value("${cdk.cloud:OCI}") Cloud defaultCloud) {
        this.defaultCloud = defaultCloud;
    }

    @NonNull
    @Override
    public RunReport process(@NonNull Actions actions, @NonNull CdkProcessOptions options) {

        ec.setDefaultCloud(defaultCloud);
        ec.setFailureMode(options.getFailureMode());

        Exception exception = null;
        try {
            if (options.getOptions().contains(CdkProcessOptions.Option.UNDEPLOY)) {
                prepareUndeployExecution(actions);
            } else {
                prepareDeployExecution(actions);
            }

            do {
                CommandsRunResult result;
                try (@SuppressWarnings("unused") ExecutionContext.Round round = ec.nextRound()) {
                    ec.setExistingComponents(existingComponentResolver.resolve(ec.getActions()));

                    actionsValidator.validate();
                    deltaResolver.resolve();
                    if (limitValidator != null) {
                        limitValidator.verify();
                    }
                    commandBuilder.build();

                    result = commandRunner.run();
                }

                if (result != null && result.errorOccurred() && ec.getFailureMode() == ROLLBACK) {
                    configureRollback(result);
                }
            } while (!ec.hasFinished());
        } catch (Exception e) {
            exception = e;
        }

        ec.close();
        return new RunReport(ec, stringCleaner, exception);
    }

    private void configureRollback(CommandsRunResult result) {
        List<Command> failedCommands = result.failedCommands();
        List<Command> successfulCommands = result.successfulCommands();

        List<Command> commandsToRollback = new ArrayList<>(failedCommands);
        commandsToRollback.addAll(successfulCommands);
        List<Action<?>> rollbackActions = rollbackBuilder.buildRollback(commandsToRollback);

        ec.setActions(new Actions(rollbackActions), "Rollback");
        // switch the fail mode
        ec.setFailureMode(LAZY_FAILURE);
    }

    private void prepareUndeployExecution(Actions actions) {
        ExistingComponents comps = existingComponentResolver.resolve(actions.getActions());
        // initialize the existing components so resources can be resolved by the Builder
        ec.setExistingComponents(comps);
        ec.setActions(deleteJobBuilder.makeDeleteActions(actions.getActions()));
    }

    private void prepareDeployExecution(Actions actions) {
        actionDependenciesConfigurer.configure(actions.getActions());
        ec.setActions(actions);
    }

    @Inject
    void setDeleteJobBuilder(DeleteJobBuilder deleteJobBuilder) {
        this.deleteJobBuilder = deleteJobBuilder;
    }

    @Inject
    void setRollbackBuilder(RollbackActionsBuilder rollbackBuilder) {
        this.rollbackBuilder = rollbackBuilder;
    }
}
