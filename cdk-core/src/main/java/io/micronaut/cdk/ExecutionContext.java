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
package io.micronaut.cdk;

import io.micronaut.cdk.action.Action;
import io.micronaut.cdk.action.Actions;
import io.micronaut.cdk.action.Executable.Result;
import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.command.Command;
import io.micronaut.cdk.command.Command.Status;
import io.micronaut.cdk.command.Commands;
import io.micronaut.cdk.command.CommandsRunResult;
import io.micronaut.cdk.component.Component;
import io.micronaut.cdk.component.ExistingComponents;
import io.micronaut.cdk.delta.Delta;
import io.micronaut.cdk.impl.ComponentsCache;
import io.micronaut.cdk.util.Assert;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Singleton;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static io.micronaut.cdk.Cloud.ANY;
import static io.micronaut.cdk.ExecutionContext.Step.CLOSED;
import static io.micronaut.cdk.ExecutionContext.Step.COMMANDS;
import static io.micronaut.cdk.ExecutionContext.Step.COMPONENTS;
import static io.micronaut.cdk.ExecutionContext.Step.DELTA;
import static io.micronaut.cdk.ExecutionContext.Step.INITIAL;
import static io.micronaut.cdk.ExecutionContext.Step.SORTED;
import static io.micronaut.cdk.util.Constants.DRY_RUN_KEY;
import static io.micronaut.core.util.StringUtils.FALSE;

/**
 * Maintains state during a run.
 */
@Singleton
public class ExecutionContext {

    /**
     * Store as a variable to indicate the value isn't known.
     */
    public static final String UNKNOWN_VARIABLE = "";

    /**
     * Default ExistingComponents container.
     */
    private static final ExistingComponents EMPTY_EXISTING_COMPONENTS = new ExistingComponents(List.of());

    /**
     * Name for the default Round.
     */
    private static final String DEFAULT_ROUND_NAME = "execution";

    /**
     * Empty actions used in DUMMY Round.
     */
    private static final Actions EMPTY_ACTIONS = new Actions();

    /**
     * A special initial dummy round. It ignores all mutations, reports it had never started, but has
     * already ended.
     */
    private static final Round DUMMY = new Round(new ExecutionContext(true), EMPTY_ACTIONS) {

        @Override
        public boolean hasStarted() {
            return false;
        }

        @Override
        public void setExistingComponents(ExistingComponents existingComponents) {
        }

        @Override
        public void setSortedCommands(Commands sortedCommands) {
        }

        @Override
        public void setDelta(Delta delta) {
        }

        @Override
        public void setCommands(Commands commands) {
        }

        @Override
        public void registerResolved(Component<?> component) {
        }

        @Override
        public void registerDeployed(Action<?> action, Component<?> deployed) {
        }

        @Override
        public void registerUndeployed(DeleteSpec<?> deleteSpec) {
        }

        @Override
        public void registerIpAddress(String cdkId, String ipAddress, boolean privateAddress) {
        }

        @Override
        public void registerVariable(String cdkId, String name, String value) {
        }

        @Override
        public void registerExecuted(Command command, List<Result> results) {
        }
    };

    /**
     * Caches and indexes Components by their CDK ID and Cloud ID. The cache is shared between rounds as
     * it represents cloud(s) state(s) known at a given time.
     */
    private final ComponentsCache componentsCache = new ComponentsCache();

    private final boolean dryRun;
    private final Map<Command, List<Result>> executed = new HashMap<>();
    private final Map<String, String> privateIpAddresses = new HashMap<>();
    private final Map<String, String> publicIpAddresses = new HashMap<>();
    private final Map<String, Map<String, String>> variablesByCdkId = new HashMap<>();

    private ExistingComponents initialExistingComponents;
    private ExistingComponents previousRoundComponents;

    /**
     * The current round.
     */
    private Round currentRound;

    /**
     * All registered rounds.
     */
    private final List<Round> rounds = new ArrayList<>();

    /**
     * The default cloud, if none is specified.
     */
    private Cloud defaultCloud;

    /**
     * Default failure mode for new Rounds.
     */
    private FailureMode defaultFailureMode = FailureMode.LAZY_FAILURE;

    /**
     * Constructor.
     *
     * @param dryRun true if a dry run, i.e. do all the work except for making changes in the cloud
     */
    public ExecutionContext(
            @Property(name = DRY_RUN_KEY, defaultValue = FALSE) boolean dryRun) {
        this.dryRun = dryRun;
        currentRound = DUMMY;
    }

    /**
     * Sets the default Cloud. This Cloud will be used in preference when no specific target is defined in a Spec.
     *
     * @param cloud the default cloud.
     */
    public void setDefaultCloud(Cloud cloud) {
        if (cloud == ANY) {
            throw new IllegalArgumentException("Can't set default cloud to ANY. Pick a specific value.");
        }
        defaultCloud = Assert.notNull(cloud, "Default cloud cannot be null");
    }

    /**
     * Returns the default cloud. This Cloud should be used, if an action does not specify the target
     * cloud.
     *
     * @return the default cloud
     */
    public Cloud getDefaultCloud() {
        return defaultCloud;
    }

    /**
     * Pre-existing Components. Unlike {@link #getExistingComponents()} that return the components that have existed
     * before the current Round, this method returns Components that had existed before the first Round started.
     *
     * @return ExistingComponents before the first operation.
     */
    public ExistingComponents getInitialExistingComponents() {
        return initialExistingComponents != null ? initialExistingComponents : EMPTY_EXISTING_COMPONENTS;
    }

    /**
     * Returns all executed actions.
     *
     * @return all executed actions.
     */
    public Map<Command, List<Result>> getAllExecuted() {
        return Map.copyOf(executed);
    }

    /**
     * Returns all public IP addresses.
     *
     * @return all public IP addresses.
     */
    public Map<String, String> getDefinedPublicIpAddresses() {
        return Map.copyOf(publicIpAddresses);
    }

    /**
     * Returns all private IP addresses.
     *
     * @return all private IP addresses.
     */
    public Map<String, String> getDefinedPrivateIpAddresses() {
        return Map.copyOf(privateIpAddresses);
    }

    /**
     * Returns all variables.
     *
     * @return all variables.
     */
    public Map<String, Map<String, String>> getDefinedVariables() {
        return copyOf(variablesByCdkId);
    }

    private static Map<String, Map<String, String>> copyOf(Map<String, Map<String, String>> variables) {
        Map<String, Map<String, String>> result = new HashMap<>();
        Set.copyOf(variables.entrySet()).forEach(entry -> {
            result.put(entry.getKey(), Map.copyOf(entry.getValue()));
        });
        return result;
    }

    /**
     * Actions.
     *
     * @return the Action container
     */
    @NonNull
    public Actions getActions() {
        return currentRound.getActions();
    }

    /**
     * The deployment failure mode.
     *
     * @return the failure mode.
     */
    public FailureMode getFailureMode() {
        return currentRound.getFailureMode();
    }

    /**
     * Set the deployment failure mode.
     *
     * @param failureMode the failure mode
     */
    public void setFailureMode(@NonNull FailureMode failureMode) {
        currentRound.setFailureMode(failureMode);
        defaultFailureMode = failureMode;
    }

    /**
     * Actions. Creates a Round with the specified set of actions.
     *
     * @param actions Actions
     */
    public void setActions(@NonNull Actions actions) {
        setActions(actions, null);
    }

    /**
     * Actions. Creates a Round with the specified set of actions.
     *
     * @param actions   Actions
     * @param roundName name for the round
     */
    public void setActions(@NonNull Actions actions, String roundName) {
        Round r = new Round(roundName, this, Assert.notNull(actions, "Actions cannot be null"));
        r.setFailureMode(defaultFailureMode);
        if (currentRound == DUMMY) {
            currentRound = r;
        }
        rounds.add(r);
    }

    /**
     * Sorted Actions.
     *
     * @return sorted Actions
     */
    public Actions getSortedActions() {
        return currentRound.getSortedActions();
    }

    /**
     * Whether it's a dry run.
     *
     * @return true if a dry run
     */
    public boolean isDryRun() {
        return dryRun;
    }

    /**
     * ExistingComponents.
     *
     * @param existingComponents ExistingComponents
     */
    public void setExistingComponents(@NonNull ExistingComponents existingComponents) {
        if (currentRound != DUMMY) {
            currentRound.setExistingComponents(existingComponents);
        }
        componentsCache.loadFrom(existingComponents);
        // record the first existing components
        if (this.initialExistingComponents == null) {
            this.initialExistingComponents = existingComponents;
        }
    }

    /**
     * ExistingComponents.
     *
     * @return ExistingComponents
     */
    public ExistingComponents getExistingComponents() {
        return currentRound.getExistingComponents();
    }

    /**
     * Delta.
     *
     * @param delta Delta
     */
    public void setDelta(@NonNull Delta delta) {
        currentRound.setDelta(delta);
    }

    /**
     * Delta.
     *
     * @return Delta
     */
    public Delta getDelta() {
        return currentRound.getDelta();
    }

    /**
     * The commands.
     *
     * @param commands Commands
     */
    public void setCommands(@NonNull Commands commands) {
        currentRound.setCommands(commands);
    }

    /**
     * The commands.
     *
     * @return Commands
     */
    public Commands getCommands() {
        return currentRound.getCommands();
    }

    /**
     * Encapsulates the result of the executed commands in a {@link CommandsRunResult} for the current round.
     *
     * @return the result of executed commands
     */
    public CommandsRunResult collectRunResults() {
        List<Command> successful = new ArrayList<>();
        List<Command> failed = new ArrayList<>();
        List<Command> skipped = new ArrayList<>();
        Map<Command, Throwable> errors = new HashMap<>();
        for (Command c : getCommands()) {
            switch (c.getCommandStatus()) {
                case SUCCESSFUL -> successful.add(c);
                case SKIPPED -> skipped.add(c);
                case FAILED -> {
                    failed.add(c);
                    errors.put(c, c.getFailureCause());
                }
                default -> throw new IllegalStateException(
                        String.format("Unexpected command status %s", c.getCommandStatus())
                );
            }
        }

        return new CommandsRunResult(successful, failed, skipped, errors);
    }

    /**
     * Set the sorted commands.
     *
     * @param sortedCommands sorted Commands
     */
    public void setSortedCommands(@NonNull Commands sortedCommands) {
        currentRound.setSortedCommands(sortedCommands);
    }

    /**
     * Register a resolved component for faster downstream lookups.
     *
     * @param component the component
     */
    public void registerResolved(@NonNull Component<?> component) {
        componentsCache.registerResolved(component);
    }

    /**
     * Resolved components.
     *
     * @return resolved components by CDK ID
     */
    public Map<String, Component<?>> getResolved() {
        return Collections.unmodifiableMap(componentsCache.getResolved());
    }

    /**
     * Registered a deployed component.
     *
     * @param action   the action
     * @param deployed the deployed component
     */
    public void registerDeployed(@NonNull Action<?> action,
                                 @NonNull Component<?> deployed) {
        componentsCache.registerDeployed(action, deployed);
    }

    /**
     * Registers a command that failed during execution.
     *
     * @param command      The command that failed.
     * @param failureCause The {@link Throwable} indicating the reason for the command's failure.
     */
    public synchronized void registerFailedCommand(@NonNull Command command, @NonNull Throwable failureCause) {
        assertStep(COMMANDS, "Cannot register failed commands");
        Assert.notNull(command, "Command cannot be null");
        Assert.notNull(failureCause, "FailureCauses cannot be null");
        command.setFailureCause(failureCause);
    }

    /**
     * Returns a list of commands that failed execution, from the current Round.
     *
     * @return A {@link List} of failed {@code Command}.
     */
    public synchronized List<Command> getFailedCommands() {
        return getCommands().stream()
                .filter(command -> command.getCommandStatus() == Status.FAILED)
                .toList();
    }

    /**
     * Register an undeployed delte spec.
     *
     * @param deleteSpec the delete spec
     */
    public void registerUndeployed(@NonNull DeleteSpec<?> deleteSpec) {
        componentsCache.registerUndeployed(deleteSpec);
    }

    /**
     * Store a public or private IP address after deploying something.
     *
     * @param cdkId          the CDK ID of the spec corresponding to the
     *                       component with the IP address
     * @param ipAddress      the IP address
     * @param privateAddress true if a private address, false if public
     */
    public void registerIpAddress(@NonNull String cdkId,
                                  @NonNull String ipAddress,
                                  boolean privateAddress) {
        currentRound.registerIpAddress(cdkId, ipAddress, privateAddress);
    }

    /**
     * Look up a private IP address for the specified CDK ID.
     *
     * @param cdkId the CDK ID
     * @return the IP address
     */
    @Nullable
    public String getPrivateIpAddress(@NonNull String cdkId) {
        return currentRound.getPrivateIpAddress(cdkId);
    }

    /**
     * Private IP addresses.
     *
     * @return all private IP addresses
     */
    @NonNull
    public Map<String, String> getPrivateIpAddresses() {
        return currentRound.getPrivateIpAddresses();
    }

    /**
     * Look up a public IP address for the specified CDK ID.
     *
     * @param cdkId the CDK ID
     * @return the IP address
     */
    @Nullable
    public String getPublicIpAddress(@NonNull String cdkId) {
        return currentRound.getPublicIpAddress(cdkId);
    }

    /**
     * Public IP addresses.
     *
     * @return all public IP addresses
     */
    @NonNull
    public Map<String, String> getPublicIpAddresses() {
        return currentRound.getPublicIpAddresses();
    }

    /**
     * Store execution variable.
     *
     * @param cdkId CDK ID
     * @param name  variable name
     * @param value variable value
     */
    public void registerVariable(@NonNull String cdkId,
                                 @NonNull String name,
                                 @NonNull String value) {
        assertStep(COMMANDS, "Cannot register variable");
        Assert.notNull(name, "name cannot be null");
        Assert.notNull(value, "value cannot be null");
        currentRound.registerVariable(cdkId, name, value);
    }

    /**
     * Gets all registered variables.
     *
     * @return the variables; keys are CDK IDs, values are registered variable values by name
     */
    public Map<String, Map<String, String>> getAllVariables() {
        return currentRound.getAllVariables();
    }

    /**
     * The registered variables for the specified CDK ID.
     *
     * @param cdkId CDK ID
     * @return the variables
     */
    @NonNull
    public Map<String, String> getVariables(@NonNull String cdkId) {
        return currentRound.getVariables(cdkId);
    }

    /**
     * The registered variable for the specified CDK ID and name.
     *
     * @param name  the name
     * @param cdkId the CDK ID
     * @return the variable
     */
    @NonNull
    public Optional<String> getVariable(@NonNull String name,
                                        @NonNull String cdkId) {
        return currentRound.getVariable(cdkId, name);
    }

    /**
     * Checks if variable exists in corresponding CDK ID.
     *
     * @param cdkId    the CDK ID
     * @param variable the variable
     * @return true if the variable exists
     */
    public boolean hasVariable(@NonNull String cdkId,
                               @NonNull String variable) {
        return currentRound.hasVariable(cdkId, variable);
    }

    /**
     * Register executed results.
     *
     * @param command a command with an Executable
     * @param results the results
     */
    public void registerExecuted(@NonNull Command command,
                                 @NonNull List<Result> results) {
        assertStep(COMMANDS, "Cannot register executed");
        currentRound.registerExecuted(command, results);
    }

    /**
     * Keys are an {@link Action} and values are the associated deployed {@link Component}s.
     *
     * @return the actions and components
     */
    @NonNull
    public Map<Action<?>, Component<?>> getActionsAndComponents() {
        return componentsCache.getActionsAndComponents();
    }

    /**
     * Components deployed during this run.
     *
     * @return components
     */
    @NonNull
    public Collection<Component<?>> getComponents() {
        return componentsCache.getComponents();
    }

    /**
     * Results keyed by commands with an Executable that have been run.
     *
     * @return results
     */
    @NonNull
    public Map<Command, List<Result>> getExecuted() {
        return currentRound.getExecuted();
    }

    /**
     * Undeployed specs.
     *
     * @return undeployed specs
     */
    @NonNull
    public List<DeleteSpec<?>> getUndeployed() {
        return componentsCache.getUndeployed();
    }

    /**
     * For tests.
     *
     * @return the current step
     */
    @NonNull
    public Step getStep() {
        return currentRound.getStep();
    }

    /**
     * Look for a component that's been resolved or deployed previously in the
     * current run to avoid unnecessary API call lookups. The call returns
     * the cloud resource.
     *
     * @param id                 the cloud id or CDK ID
     * @param byCdkId            whether the id is CDK or cloud id
     * @param cloudComponentType the class of the cloud component in the <code>Component</code>
     * @param <C>                the type of the cloud component
     * @return the cloud component if found
     */
    @NonNull
    public <C> Optional<C> findDeployed(@NonNull String id,
                                        boolean byCdkId,
                                        @NonNull Class<C> cloudComponentType) {
        return componentsCache.findDeployed(id, byCdkId, cloudComponentType);
    }

    /**
     * Look for a component that's been resolved or deployed previously in the
     * current run to avoid unnecessary API call lookups. This call returns CDK {@link Component}.
     * Returns {@code Optional.empty}, if the actual component that matches the ID does not correspond
     * to the desired type.
     *
     * @param id                 the cloud id or CDK ID
     * @param byCdkId            whether the id is CDK or cloud id
     * @param cloudComponentType the class of the CDK component
     * @param <C>                the type of the component
     * @return the CDK Component if found
     */
    @SuppressWarnings("rawtypes")
    @NonNull
    public <C extends Component> Optional<C> findDeployedComponent(@NonNull String id,
                                                                   boolean byCdkId,
                                                                   @NonNull Class<C> cloudComponentType) {
        return componentsCache.findDeployedComponent(id, byCdkId, cloudComponentType);
    }

    /**
     * Returns the current round. The current round may not be started yet (see {@link Round#hasStarted()}, or can
     * be already finished (see {@link Round#hasFinished()}.
     *
     * @return current round
     */
    @NonNull
    public Round getCurrentRound() {
        return currentRound;
    }

    /**
     * Attempts to start a new Round. Initiates the new Round and returns its instance. If the current round is
     * the last one and was finished, it is returned in its finished state.
     *
     * @return the next round.
     */
    public synchronized Round nextRound() {
        if (currentRound == DUMMY) {
            if (rounds.isEmpty()) {
                return currentRound;
            }
            currentRound = rounds.get(0);
        }
        if (currentRound.hasFinished()) {
            int index = rounds.indexOf(currentRound);
            if (currentRound != rounds.get(rounds.size() - 1)) {
                currentRound = rounds.get(index + 1);
            } else {
                return currentRound;
            }
        } else {
            return currentRound;
        }
        if (!currentRound.hasStarted()) {
            currentRound.start();
        }
        return currentRound;
    }

    /**
     * Determines if the work has finished. Execution finishes, if the last round is in {@link Round#hasFinished}
     * state.
     *
     * @return True, if the execution has finished.
     */
    public boolean hasFinished() {
        if (currentRound == DUMMY) {
            return true;
        }
        if (currentRound == rounds.get(rounds.size() - 1)) {
            return currentRound.hasFinished();
        } else {
            return false;
        }
    }

    /**
     * Returns all configured {@link Round}s.
     *
     * @return execution rounds.
     */
    public List<Round> getExecutionRounds() {
        return List.copyOf(rounds);
    }

    /**
     * Marks the round execution as finished.
     */
    public void close() {
        currentRound.close();
    }

    /**
     * The number of elapsed milliseconds.
     *
     * @return the milliseconds
     */
    public int getRuntimeMillis() {
        return currentRound.getRuntimeMillis();
    }

    /**
     * Computes the total time of the execution. Returns the time between first round's start and
     * last round's end. If rounds are not finished, it returns the time up to now. If no round
     * has started, it returns -1.
     *
     * @return total execution time.
     */
    public int getTotalTimeMillis() {
        Round first = rounds.get(0);
        Round last = rounds.get(rounds.size() - 1);
        if (first.hasStarted()) {
            return (int) (last.getEndTime() - first.getStartTime());
        } else {
            return -1;
        }
    }

    /**
     * Collects outcome of a Round. Called internally. The method will merge the Round's data into this ExecutionContext
     * data
     *
     * @param round the round to collect data from.
     */
    void collect(Round round) {
        this.publicIpAddresses.putAll(round.getPublicIpAddresses());
        this.privateIpAddresses.putAll(round.getPrivateIpAddresses());
        this.executed.putAll(round.getExecuted());
        this.variablesByCdkId.putAll(round.getAllVariables());
    }

    private void assertStep(@NonNull Step step,
                            @NonNull String messageStart) {
        Assert.state(getStep() == step, messageStart + " at step " + getStep());
    }

    /**
     * Tracks the processing step.
     */
    public enum Step {
        /**
         * After construction, before setting ExistingComponents.
         */
        INITIAL,

        /**
         * After setting ExistingComponents.
         */
        COMPONENTS,

        /**
         * After setting the Delta.
         */
        DELTA,

        /**
         * After setting Commands.
         */
        COMMANDS,

        /**
         * After setting sorted Commands.
         */
        SORTED,

        /**
         * After closing.
         */
        CLOSED
    }

    /**
     * One round of actions.
     */
    public static class Round implements Closeable {
        private final ExecutionContext executionContext;
        private final String name;
        private final Actions actions;

        // will be published to ExecutionContext after Round completes
        private final Map<Command, List<Result>> executed = new ConcurrentHashMap<>();
        private final Map<String, String> privateIpAddresses = new ConcurrentHashMap<>();
        private final Map<String, String> publicIpAddresses = new ConcurrentHashMap<>();
        private final Map<String, Map<String, String>> variablesByCdkId = new ConcurrentHashMap<>();
        private ExistingComponents existingComponents = EMPTY_EXISTING_COMPONENTS;
        private long startTime = -1;

        private FailureMode failureMode;
        private Commands commands;
        private Delta delta;
        private long endTime;
        private Actions sortedActions;
        private Commands sortedCommands;
        private Step step = INITIAL;

        /**
         * Creates a named Round.
         *
         * @param name    name
         * @param context execution context instance
         * @param actions actions for the round.
         */
        Round(String name, ExecutionContext context, Actions actions) {
            this.name = name == null ? DEFAULT_ROUND_NAME : name;
            this.actions = actions;
            this.executionContext = context;
        }

        /**
         * Creates a round with a default name.
         *
         * @param context the execution context instance
         * @param actions actions for the round.
         */
        Round(ExecutionContext context, Actions actions) {
            this(DEFAULT_ROUND_NAME, context, actions);
        }

        /**
         * Returns name of the round. The implicit round has name {@link #DEFAULT_ROUND_NAME}.
         *
         * @return round name.
         */
        public String getName() {
            return name;
        }

        /**
         * Determines if the round has started already.
         *
         * @return true, if the round has started
         */
        public boolean hasStarted() {
            return step != INITIAL;
        }

        /**
         * Determines if the round has been finished.
         *
         * @return true, if the round has been finished.
         */
        public boolean hasFinished() {
            return this == DUMMY || step == CLOSED;
        }

        /**
         * Starts the round. Creates snapshot of variables from the execution context, initializes round processing.
         * Must be called before {@link Step#COMPONENTS} step. It should be called by ExecutionContext / managing
         * code only.
         */
        void start() {
            assertStep(INITIAL, "Start must be called before COMPONENTS");
            privateIpAddresses.putAll(executionContext.privateIpAddresses);
            publicIpAddresses.putAll(executionContext.publicIpAddresses);
            variablesByCdkId.putAll(executionContext.variablesByCdkId);
            startTime = System.currentTimeMillis();
        }

        /**
         * Actions.
         *
         * @return the Action container
         */
        public Actions getActions() {
            return actions;
        }

        /**
         * The deployment failure mode for the current round.
         *
         * @return the failure mode
         */
        public FailureMode getFailureMode() {
            return failureMode;
        }

        /**
         * Set the deployment failure mode for the current round.
         *
         * @param failureMode the failure mode
         */
        public void setFailureMode(@NonNull FailureMode failureMode) {
            Assert.notNull(failureMode, "FailureMode cannot be null");
            this.failureMode = failureMode;
        }

        /**
         * Returns the timestamp of Round's start.
         *
         * @return start time
         */
        public long getStartTime() {
            return startTime;
        }

        /**
         * Returns commands.
         *
         * @return commands.
         */
        public Commands getCommands() {
            return commands;
        }

        /**
         * Returns the delta.
         *
         * @return delta.
         */
        public Delta getDelta() {
            return delta;
        }

        /**
         * Returns the ExistingComponents for this Round.
         *
         * @return ExistingComponents instance.
         */
        public ExistingComponents getExistingComponents() {
            return existingComponents;
        }

        /**
         * Returns sorted actions.
         *
         * @return sorted actions.
         */
        public Actions getSortedActions() {
            return sortedActions;
        }

        /**
         * Returns sorted commands.
         *
         * @return sorted commands.
         */
        public Commands getSortedCommands() {
            return sortedCommands;
        }

        /**
         * Returns the current step.
         *
         * @return current step.
         */
        public Step getStep() {
            return step;
        }

        /**
         * Sets existing components. All components will be tracked
         * as resolved by the ExecutionContext's component cache.
         *
         * @param existingComponents existing components instance.
         */
        public void setExistingComponents(@NonNull ExistingComponents existingComponents) {
            assertStep(INITIAL, "Cannot set ExistingComponents");
            if (this.existingComponents != EMPTY_EXISTING_COMPONENTS) {
                throw new IllegalStateException("Cannot overwrite ExistingComponents");
            }
            this.existingComponents = Assert.notNull(existingComponents, "ExistingComponents cannot be null");
            step = COMPONENTS;
        }

        /**
         * Delta.
         *
         * @param delta Delta
         */
        public void setDelta(@NonNull Delta delta) {
            assertStep(COMPONENTS, "Cannot set Delta");
            Assert.isNull(this.delta, "Cannot overwrite Delta");

            this.delta = Assert.notNull(delta, "Delta cannot be null");
            step = DELTA;
        }

        /**
         * The commands.
         *
         * @param commands Commands
         */
        public void setCommands(@NonNull Commands commands) {
            assertStep(DELTA, "Cannot set Commands");
            Assert.isNull(this.commands, "Cannot overwrite Commands");

            this.commands = Assert.notNull(commands, "Commands cannot be null");
            step = COMMANDS;
        }

        /**
         * Set the sorted commands.
         *
         * @param sortedCommands sorted Commands
         */
        public void setSortedCommands(@NonNull Commands sortedCommands) {
            assertStep(COMMANDS, "Cannot set sorted Commands");
            Assert.isNull(this.sortedCommands, "Cannot overwrite sorted Commands");

            this.sortedCommands = Assert.notNull(sortedCommands, "sorted Commands cannot be null");
            step = SORTED;

            determineSortedActions();
        }

        private void determineSortedActions() {
            sortedActions = new Actions(sortedCommands
                    .stream()
                    .map(Command::getAction)
                    .toList());
        }

        /**
         * Register a resolved component for faster downstream lookups.
         *
         * @param component the component
         */
        public void registerResolved(@NonNull Component<?> component) {
            executionContext.componentsCache.registerResolved(component);
        }

        /**
         * Registered a deployed component.
         *
         * @param action   the action
         * @param deployed the deployed component
         */
        public void registerDeployed(@NonNull Action<?> action,
                                     @NonNull Component<?> deployed) {
            assertStep(COMMANDS, "Cannot register deployed");
            Assert.notNull(action, "Action cannot be null");
            Assert.notNull(deployed, "Deployed component cannot be null");
            executionContext.componentsCache.registerDeployed(action, deployed);
        }

        /**
         * Register an undeployed delte spec.
         *
         * @param deleteSpec the delete spec
         */
        public void registerUndeployed(@NonNull DeleteSpec<?> deleteSpec) {
            assertStep(COMMANDS, "Cannot register undeployed");
            executionContext.componentsCache.registerUndeployed(deleteSpec);
        }

        /**
         * Store a public or private IP address after deploying something.
         *
         * @param cdkId          the CDK ID of the spec corresponding to the
         *                       component with the IP address
         * @param ipAddress      the IP address
         * @param privateAddress true if a private address, false if public
         */
        public void registerIpAddress(@NonNull String cdkId,
                                      @NonNull String ipAddress,
                                      boolean privateAddress) {
            assertStep(COMMANDS, "Cannot register IP address");
            Assert.notNull(ipAddress, "ipAddress cannot be null");
            if (privateAddress) {
                privateIpAddresses.put(cdkId, ipAddress);
            } else {
                publicIpAddresses.put(cdkId, ipAddress);
            }
        }

        /**
         * Look up a private IP address for the specified CDK ID.
         *
         * @param cdkId the CDK ID
         * @return the IP address
         */
        @Nullable
        public String getPrivateIpAddress(@NonNull String cdkId) {
            return privateIpAddresses.get(cdkId);
        }

        /**
         * Private IP addresses.
         *
         * @return all private IP addresses
         */
        @NonNull
        public Map<String, String> getPrivateIpAddresses() {
            return Collections.unmodifiableMap(privateIpAddresses);
        }

        /**
         * Look up a public IP address for the specified CDK ID.
         *
         * @param cdkId the CDK ID
         * @return the IP address
         */
        @Nullable
        public String getPublicIpAddress(@NonNull String cdkId) {
            return publicIpAddresses.get(cdkId);
        }

        /**
         * Public IP addresses.
         *
         * @return all public IP addresses
         */
        @NonNull
        public Map<String, String> getPublicIpAddresses() {
            return Map.copyOf(publicIpAddresses);
        }

        /**
         * Store execution variable.
         *
         * @param cdkId CDK ID
         * @param name  variable name
         * @param value variable value
         */
        public void registerVariable(@NonNull String cdkId,
                                     @NonNull String name,
                                     @NonNull String value) {
            assertStep(COMMANDS, "Cannot register variable");
            Assert.notNull(name, "name cannot be null");
            Assert.notNull(value, "value cannot be null");
            variablesByCdkId.computeIfAbsent(cdkId, it -> new HashMap<>()).put(name, value);
        }

        /**
         * Gets all registered variables.
         *
         * @return the variables; keys are CDK IDs, values are registered variable values by name
         */
        public Map<String, Map<String, String>> getAllVariables() {
            return copyOf(variablesByCdkId);
        }

        /**
         * The registered variables for the specified CDK ID.
         *
         * @param cdkId CDK ID
         * @return the variables
         */
        @NonNull
        public Map<String, String> getVariables(@NonNull String cdkId) {
            /// TODO possibly not thread-safe DURING execution of a command,
            // but shuld be OK after the command completes and is consumed.
            return Map.copyOf(variablesByCdkId.getOrDefault(cdkId, Map.of()));
        }

        /**
         * The registered variable for the specified CDK ID and name.
         *
         * @param name  the name
         * @param cdkId the CDK ID
         * @return the variable
         */
        @NonNull
        public Optional<String> getVariable(@NonNull String name,
                                            @NonNull String cdkId) {
            // prevent copy
            return Optional.ofNullable(variablesByCdkId.getOrDefault(cdkId, Map.of()).get(name));
        }

        /**
         * Checks if variable exists in corresponding CDK ID.
         *
         * @param cdkId    the CDK ID
         * @param variable the variable
         * @return true if the variable exists
         */
        public boolean hasVariable(@NonNull String cdkId,
                                   @NonNull String variable) {
            Map<String, String> variables = variablesByCdkId.get(cdkId);
            return variables != null && variables.containsKey(variable);
        }

        /**
         * Register executed results.
         *
         * @param command a command with an Executable
         * @param results the results
         */
        public void registerExecuted(@NonNull Command command,
                                     @NonNull List<Result> results) {
            assertStep(COMMANDS, "Cannot register executed");

            executed.put(Assert.notNull(command, "Command cannot be null"),
                    Assert.notNull(results, "results cannot be null"));
        }

        /**
         * Keys are an {@link Action} and values are the associated deployed {@link Component}s.
         *
         * @return the actions and components
         */
        @NonNull
        public Map<Action<?>, Component<?>> getActionsAndComponents() {
            return executionContext.componentsCache.getActionsAndComponents();
        }

        /**
         * Returns a Component associated with an action, or {@code null}. The component may be changed
         * or deleted after the action completed, in case the method returns {@code null}, unless {@code includeObsolete}
         * is set to {@code true}.
         *
         * @param action          the action
         * @param includeObsolete if true, returns Components that have been changed or deleted by subsequent action(s)
         * @return Component associated with action or {@code null}.
         */
        @Nullable
        public Component<?> getActionComponent(Action<?> action, boolean includeObsolete) {
            return executionContext.componentsCache.getActionComponent(action, includeObsolete);
        }

        /**
         * Components deployed during this run.
         *
         * @return components
         */
        @NonNull
        public Collection<Component<?>> getComponents() {
            return executionContext.getComponents();
        }

        /**
         * Results keyed by commands with an Executable that have been run.
         *
         * @return results
         */
        @NonNull
        public Map<Command, List<Result>> getExecuted() {
            return Map.copyOf(executed);
        }

        /**
         * The number of elapsed milliseconds.
         *
         * @return the milliseconds
         */
        public int getRuntimeMillis() {
            if (startTime <= 0) {
                return -1;
            }
            return (int) (getEndTime() - startTime);
        }

        /**
         * Returns this round's end time.
         *
         * @return end time of this round.
         */
        long getEndTime() {
            if (step == CLOSED) {
                return endTime;
            }
            return System.currentTimeMillis();
        }

        /**
         * Undeployed specs. Unlike {@link ExecutionContext#getUndeployed()}, only undeployed actions
         * contained in this Round are returned.
         *
         * @return undeployed specs
         */
        @NonNull
        public List<DeleteSpec<?>> getUndeployed() {
            Set<DeleteSpec<?>> undeployed = new LinkedHashSet<>(executionContext.getUndeployed());
            undeployed.retainAll(actions.getActions());
            return List.copyOf(undeployed);
        }

        /**
         * Look for a component that's been resolved or deployed previously in the
         * current run to avoid unnecessary API call lookups.
         *
         * @param id                 the cloud id or CDK ID
         * @param byCdkId            whether the id is CDK or cloud id
         * @param cloudComponentType the class of the cloud component in the <code>Component</code>
         * @param <C>                the type of the cloud component
         * @return the cloud component if found
         */
        @NonNull
        public <C> Optional<C> findDeployed(@NonNull String id,
                                            boolean byCdkId,
                                            @NonNull Class<C> cloudComponentType) {
            return executionContext.findDeployed(id, byCdkId, cloudComponentType);
        }

        /**
         * Mark the execution as finished.
         */
        @Override
        public void close() {
            endTime = System.currentTimeMillis();
            step = CLOSED;
            executionContext.collect(this);
        }

        private void assertStep(@NonNull Step step,
                                @NonNull String messageStart) {
            Assert.state(getStep() == step, messageStart + " at step " + this.step);
        }

        /**
         * Sets the current step. This method is only intended for testing.
         *
         * @param step the new step value.
         */
        // only for testing
        void setStep(@NonNull Step step) {
            this.step = step;
        }

        /**
         * Failed commands in this Round.
         *
         * @return failed commands.
         */
        public synchronized List<Command> getFailedCommands() {
            return commands == null ? List.of() : getCommands().stream()
                    .filter(command -> command.getCommandStatus() == Status.FAILED)
                    .toList();
        }
    }
}
