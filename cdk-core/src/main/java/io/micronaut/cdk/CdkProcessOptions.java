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

import io.micronaut.core.annotation.NonNull;

import java.util.Arrays;
import java.util.EnumSet;

import static io.micronaut.cdk.FailureMode.LAZY_FAILURE;

/**
 * Settings for CDK process.
 * <p>
 * If an option is not explicitly set, the default will be used.
 * <ul>
 *   <li>{@code checkLimits} defaults to {@code true}</li>
 *   <li>{@code dryRun} defaults to {@code false}</li>
 *   <li>{@code failureMode} defaults to {@link FailureMode#LAZY_FAILURE}</li>
 * </ul>
 */
public final class CdkProcessOptions {

    /**
     * The default failure mode used when none is specified.
     */
    private static final FailureMode DEFAULT_FAILURE_MODE = LAZY_FAILURE;

    /**
     * Whether to check limits before deploying.
     */
    private boolean checkLimits = true;

    /**
     * Whether to execute in dry-run mode.
     */
    private boolean dryRun;

    /**
     * The mode determining how failures are handled during execution.
     */
    private FailureMode failureMode = DEFAULT_FAILURE_MODE;

    private final EnumSet<Option> options = EnumSet.noneOf(Option.class);

    /**
     * Whether to check limits before deploying.
     *
     * @return true if limit checking is enabled
     */
    public boolean getCheckLimits() {
        return checkLimits;
    }

    /**
     * Whether to check limits before deploying.
     *
     * @param checkLimits true to enable limit checking
     * @return this, for method chaining
     */
    public CdkProcessOptions checkLimits(boolean checkLimits) {
        this.checkLimits = checkLimits;
        return this;
    }

    /**
     * Whether the process should execute in dry-run mode.
     * If true, do all the work except for making changes in the cloud.
     *
     * @return true if dry-run mode is enabled
     */
    public boolean isDryRun() {
        return dryRun;
    }

    /**
     * Whether the process should execute in dry-run mode.
     * If true, do all the work except for making changes in the cloud.
     *
     * @param dryRun true to enable dry-run mode
     * @return this, for method chaining
     */
    public CdkProcessOptions dryRun(boolean dryRun) {
        this.dryRun = dryRun;
        return this;
    }

    /**
     * The failure mode that determines how errors are handled during execution.
     *
     * @return the failure mode
     */
    public FailureMode getFailureMode() {
        return failureMode;
    }

    /**
     * Sets the failure mode for handling errors during execution.
     *
     * @param failureMode the {@link FailureMode} to use
     * @return this, for method chaining
     */
    public CdkProcessOptions failureMode(FailureMode failureMode) {
        this.failureMode = failureMode == null ? DEFAULT_FAILURE_MODE : failureMode;
        return this;
    }

    /**
     * Sets the specified options. If an option is already set, its state does not change.
     *
     * @param options options to set.
     * @return this, for method chaining
     */
    public CdkProcessOptions withOptions(@NonNull Option... options) {
        this.options.addAll(Arrays.asList(options));
        return this;
    }

    /**
     * Returns the options.
     *
     * @return the options
     */
    @NonNull
    public EnumSet<Option> getOptions() {
        return options;
    }

    /**
     * Represents various flags and options. Options are set through the {@link CdkProcessOptions} object.
     */
    public enum Option {

        /**
         * Attempts to undeploy all resources that would be normally created or updated by the script.
         * Reversal of execute action heavily depends on custom support (currently: none).
         */
        UNDEPLOY
    }
}
