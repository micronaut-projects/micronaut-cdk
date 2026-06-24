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

import io.micronaut.cdk.Cloud;
import io.micronaut.cdk.ExecutionContext;
import io.micronaut.cdk.action.Actions;
import io.micronaut.cdk.action.Executable.Result;
import io.micronaut.cdk.action.GroupAction;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.component.Component;
import io.micronaut.cdk.security.UserInfoHolder;
import io.micronaut.cdk.util.Assert;
import io.micronaut.cdk.util.StringCleaner;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents results from a run.
 */
public class RunReport {

    private final ExecutionContext ec;
    private final Exception exception;
    private final StringCleaner stringCleaner;

    /**
     * Constructor.
     *
     * @param ec            execution context
     * @param stringCleaner string cleaner
     * @param exception     exception if one occurred when running
     */
    public RunReport(@NonNull ExecutionContext ec,
                     @NonNull StringCleaner stringCleaner,
                     @Nullable Exception exception) {
        this.ec = Assert.notNull(ec, "ExecutionContext cannot be null");
        this.stringCleaner = Assert.notNull(stringCleaner, "StringCleaner cannot be null");
        this.exception = exception;
    }

    private void reportExecutionRound(StringBuilder sb, ExecutionContext.Round r, boolean last) {
        int seconds = ec.getRuntimeMillis() / 1000;
        int minutes = seconds / 60;
        int hours = minutes / 60;
        int days = hours / 24;
        sb.append("Run time: ");
        sb.append(String.format("%d:%02d:%02d:%02d", days, hours % 24, minutes % 60, seconds % 60));
        sb.append('\n');

        String sorted;
        Actions actions;
        if (r.getSortedActions() == null) {
            actions = r.getActions();
            sorted = "not sorted";
        } else {
            actions = r.getSortedActions();
            sorted = "sorted";
        }

        sb.append("\nActions (").append(sorted).append("):\n");
        for (var action : actions) {
            if (action instanceof GroupAction<?> g && g.isInternal()) {
                continue;
            }
            sb.append("\n\t").append(action).append('\n');
            if (action instanceof Spec) {
                Component<?> component = r.getActionComponent(action, true);
                sb.append("\n\t\tDeployed component: ");
                if (component == null) {
                    sb.append("None");
                } else {
                    sb.append(fixToString(component.getCloudComponent()));
                }
                sb.append('\n');
            }
        }

        sb.append("\nCommands:\n");
        if (r.getCommands() != null) {
            for (Command command : r.getCommands()) {
                if (command.getAction() instanceof GroupAction<?> g && g.isInternal()) {
                    continue;
                }
                sb.append("\n\t").append(command.toCompactString()).append(": ").append(command.getCommandStatus()).append('\n');
            }
        }

        sb.append("\nExistingComponents:");
        if (r.getExistingComponents() == null || r.getExistingComponents().isEmpty()) {
            sb.append(" None");
        } else {
            sb.append("\n\n\t").append(r.getExistingComponents().stream()
                    .map(this::componentToString)
                    .collect(Collectors.joining("\n\n\t"))
            );
        }
        sb.append('\n');

        sb.append("\nExecutions:");
        if (r.getExecuted().isEmpty()) {
            sb.append(" None\n");
        } else {
            for (Map.Entry<Command, List<Result>> e : r.getExecuted().entrySet()) {
                var command = e.getKey();
                var results = e.getValue();
                sb.append("\n\n\tCommand: ").append(command.toCompactString());
                sb.append("\n\n\t\tResults:\n\n\t\t\t").append(results.stream()
                        .map(Result::toString)
                        .collect(Collectors.joining("\n\n\t\t\t"))
                );
            }
        }
        sb.append("\n\n");

        sb.append("\nUndeployed:");
        if (r.getUndeployed().isEmpty()) {
            sb.append(" None\n");
        } else {
            sb.append("\n\n\t").append(r.getUndeployed().stream()
                    .map(DeleteSpec::toString)
                    .collect(Collectors.joining("\n\n\t"))
            );
        }
        sb.append("\n\n");

        sb.append("Private IP Addresses: ").append(r.getPrivateIpAddresses()).append("\n\n");
        sb.append("Public IP Addresses: ").append(r.getPublicIpAddresses()).append("\n\n");
        sb.append("Variables: ").append(r.getAllVariables()).append("\n\n");

        sb.append("Failure: ");
        boolean errors = false;
        if (last) {
            if (exception != null) {
                var sw = new StringWriter();
                exception.printStackTrace(new PrintWriter(sw));
                sb.append(exception.getMessage()).append('\n');
                sb.append(sw).append('\n');
                errors = true;

            }
        }
        if (r.getFailedCommands() != null) {
            for (Command command : r.getFailedCommands()) {
                errors = true;
                sb.append("\nError running '").append(command.toCompactString()).append("'.\n> ");
                StringWriter sw = new StringWriter();
                command.getFailureCause().printStackTrace(new PrintWriter(sw));
                sb.append(sw).append("\n");
            }
        }
        if (!errors) {
            sb.append("None\n");
        }
    }

    @Override
    public String toString() {
        Assert.authenticated();
        Cloud cloud = UserInfoHolder.get().getCloud();

        var sb = new StringBuilder("RunReport:\n\n");
        sb.append("Dry run: ").append(ec.isDryRun()).append('\n');
        sb.append("Cloud: ").append(cloud).append('\n');

        int seconds = ec.getRuntimeMillis() / 1000;
        int minutes = seconds / 60;
        int hours = minutes / 60;
        int days = hours / 24;
        sb.append("Run time: ");
        sb.append(String.format("%d:%02d:%02d:%02d", days, hours % 24, minutes % 60, seconds % 60));
        sb.append('\n');

        List<ExecutionContext.Round> rounds = ec.getExecutionRounds();
        for (ExecutionContext.Round r : rounds) {
            if (rounds.size() > 1) {
                sb.append("\n\n**** ").append(r.getName() == null ? "Script execution" : r.getName()).append(" ***\n");
            }
            reportExecutionRound(sb, r, r == rounds.get(rounds.size() - 1));
        }
        sb.append("\n\n");
        return sb.toString();
    }

    private String componentToString(@NonNull Component<?> component) {
        return component.getClass().getSimpleName() +
                "{cdkId='" + component.getCdkId() + "', " +
                "cloudComponent=" + fixToString(component.getCloudComponent()) + '}';
    }

    private String fixToString(Object component) {
        return stringCleaner.clean(component.toString());
    }

    /**
     * Execution context.
     *
     * @return the context
     */
    @NonNull
    public ExecutionContext getContext() {
        return ec;
    }

    /**
     * Exception.
     *
     * @return the exception if present
     */
    @Nullable
    public Exception getException() {
        return exception;
    }
}
