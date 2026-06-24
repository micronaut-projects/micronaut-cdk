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

import io.micronaut.cdk.Identified;
import io.micronaut.cdk.action.Action;
import io.micronaut.cdk.action.Executable;
import io.micronaut.cdk.action.GroupAction;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.component.Component;
import io.micronaut.cdk.util.Assert;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

/**
 * Contains information to create, update, or delete, or run an {@link Executable}.
 * <p>
 * A "create" command has a {@link Spec} to deploy a new instance, an "update"
 * command also has an existing {@link Component} to determine what needs to be
 * updated, and a "delete" command has either a CDK ID or cloud ID for
 * un-deploying.
 */
public final class Command {

    private final Component<?> component;
    private final DeleteSpec<?> deleteSpec;
    private final Executable<?> executable;
    private final Spec<?> spec;
    private final Action<?> action;
    private final boolean create;

    private volatile Status commandStatus;
    private volatile Throwable failureCause;

    private Command(@NonNull Spec<?> spec,
                    boolean create) {
        this.spec = Assert.notNull(spec, "Spec cannot be null");
        component = null;
        deleteSpec = null;
        executable = null;
        action = null;
        this.create = create;
        commandStatus = Status.INITIAL;
        failureCause = null;
    }

    private Command(@NonNull Action<?> action) {
        spec = null;
        component = null;
        deleteSpec = null;
        executable = null;
        this.action = action;
        create = false;
        commandStatus = Status.INITIAL;
        failureCause = null;
    }

    private Command(@NonNull DeleteSpec<?> deleteSpec,
                    @Nullable Component<?> component) {
        this.deleteSpec = Assert.notNull(deleteSpec, "DeleteSpec cannot be null");
        this.component = component;
        executable = null;
        spec = null;
        action = null;
        create = false;
    }

    private Command(@NonNull Spec<?> spec,
                    @NonNull Component<?> component) {
        this.spec = Assert.notNull(spec, "Spec cannot be null");
        this.component = Assert.notNull(component, "Component cannot be null");
        deleteSpec = null;
        executable = null;
        action = null;
        create = false;
    }

    private Command(@NonNull Executable<?> executable) {
        this.executable = Assert.notNull(executable, "Executable cannot be null");
        component = null;
        deleteSpec = null;
        spec = null;
        action = null;
        create = false;
    }

    /**
     * Creates a "create" command; deploy an instance based on the spec.
     *
     * @param spec the spec
     * @return the command
     */
    public static Command create(@NonNull Spec<?> spec) {
        return new Command(spec, true);
    }

    /**
     * Creates a "delete" command; un-deploy the existing component based on the action.
     *
     * @param deleteSpec the spec
     * @param component  the component
     * @return the command
     */
    public static Command delete(@NonNull DeleteSpec<?> deleteSpec,
                                 @Nullable Component<?> component) {
        return new Command(deleteSpec, component);
    }

    /**
     * Creates an "update" command; change the existing component based on the action.
     *
     * @param spec      the spec
     * @param component the component
     * @return the command
     */
    public static Command update(@NonNull Spec<?> spec,
                                 @NonNull Component<?> component) {
        return new Command(spec, component);
    }

    /**
     * Creates a "no-op" command; no action needed.
     *
     * @param action the spec
     * @return the command
     */
    public static Command noop(@NonNull Spec<?> action) {
        return new Command(action, false);
    }

    /**
     * Creates a "no-op" command; no action needed.
     *
     * @param action the spec
     * @return the command
     */
    public static Command noop(@NonNull Action<?> action) {
        return new Command(action);
    }

    /**
     * Creates an "execute" command; execute the command based on the executable.
     *
     * @param executable the executable
     * @return the command
     */
    public static Command execute(@NonNull Executable<?> executable) {
        return new Command(executable);
    }

    /**
     * The action.
     *
     * @return the action
     */
    @NonNull
    public Action<?> getAction() {
        return deleteSpec != null ? deleteSpec
                : (executable != null ? executable
                : (spec != null ? spec : action));
    }

    /**
     * Component.
     *
     * @return the component
     */
    @Nullable
    public Component<?> getComponent() {
        return component;
    }

    /**
     * Delete spec.
     *
     * @return the delete spec
     */
    @Nullable
    public DeleteSpec<?> getDeleteSpec() {
        return deleteSpec;
    }

    /**
     * Executable.
     *
     * @return the executable
     */
    @Nullable
    public Executable<?> getExecutable() {
        return executable;
    }

    /**
     * Spec.
     *
     * @return the spec
     */
    @Nullable
    public Spec<?> getSpec() {
        return spec;
    }

    /**
     * Whether this is a create command.
     *
     * @return true if a create action
     */
    public boolean isCreate() {
        return spec != null && component == null && create;
    }

    /**
     * Whether this is an executable command.
     *
     * @return true if executable
     */
    public boolean isExecutable() {
        return executable != null;
    }

    /**
     * Whether this is an update command.
     *
     * @return true if an update, i.e. the spec and component are not null
     */
    public boolean isUpdate() {
        return spec != null && component != null;
    }

    /**
     * Whether this is a delete command.
     *
     * @return true if delete
     */
    public boolean isDelete() {
        return deleteSpec != null;
    }

    /**
     * Whether this is a generic action command.
     *
     * @return true, if generic action.
     */
    public boolean isGenericAction() {
        return action != null;
    }

    /**
     * Return the command status.
     *
     * @return {@link Status}.
     */
    public Status getCommandStatus() {
        return commandStatus;
    }

    /**
     * Set command status.
     *
     * @param commandStatus {@link Status}.
     */
    public void setCommandStatus(@NonNull Status commandStatus) {
        Assert.notNull(commandStatus, "CommandStatus cannot be null");
        this.commandStatus = commandStatus;
    }

    /**
     * Return   command's failure.
     *
     * @return a {@link Throwable} indicating the reason for the command's failure.
     */
    public Throwable getFailureCause() {
        return failureCause;
    }

    /**
     * Set command failure cause.
     *
     * @param failureCause command's failure.
     */
    public void setFailureCause(Throwable failureCause) {
        Assert.notNull(failureCause, "FailureCause cannot be null");
        this.failureCause = failureCause;
        setCommandStatus(Status.FAILED);
    }

    @Override
    public String toString() {
        return toString(false);
    }

    /**
     * Same as toString except replace full toString of component / executable / spec
     * with class name and CDK ID.
     *
     * @return compact toString
     */
    public String toCompactString() {
        return toString(true);
    }

    private String toString(boolean compact) {

        if (isDelete()) {
            return "Command(" + deleteSpec + ')' +
                    ", Component: " + toString(component, compact);
        }

        if (isExecutable()) {
            return "Command(execute): " + toString(executable, compact);
        }

        if (isUpdate()) {
            return "Command(update): " + toString(spec, compact) +
                    ", Component: " + toString(component, compact);
        }

        if (isCreate()) {
            return "Command(create): " + toString(spec, compact);
        }

        if (isGenericAction()) {
            return "Command(action): " + toString(action, compact);
        }

        return "Command(no-op): " + toString(spec, compact);
    }

    private String toString(@Nullable Identified identified,
                            boolean compact) {
        if (identified == null) {
            return "<none>";
        }

        return compact
                ? identified.getClass().getSimpleName() + '(' + identified.getCdkId() + ')'
                : identified.toString();
    }

    /**
     * The command is a no-op and was never executed, although it finished successfully.
     *
     * @return true, if the command is a no-op
     */
    // TODO this will not work for a no-op update. Reimplement as a flag with update support.
    public boolean isNoOp() {
        return (spec != null || (action instanceof GroupAction)) && !create && commandStatus == Status.SUCCESSFUL;
    }

    /**
     * Command status.
     */
    public enum Status {
        /**
         * Initial status of a command.
         */
        INITIAL,

        /**
         * Command planned to run.
         */
        SCHEDULED,

        /**
         * Command is not SCHEDULED yet because its dependencies haven't finished.
         */
        WAITING,

        /**
         * Command is running.
         */
        RUNNING,

        /**
         * Command completed successfully.
         */
        SUCCESSFUL,

        /**
         * Command threw an exception or explicitly failed.
         */
        FAILED,

        /**
         * Command was skipped due to a failed dependency.
         */
        SKIPPED
    }
}
