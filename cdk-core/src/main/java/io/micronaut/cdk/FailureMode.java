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

/**
 * Controls the behavior of the deployment process when encountering failures
 * and how later actions are treated.
 */
public enum FailureMode {

    /**
     * Immediately stops the deployment process on the first failure encountered.
     *
     * <p>When a failure occurs:
     * <ul>
     *   <li>The deployment process halts immediately</li>
     *   <li>No further actions are executed</li>
     *   <li>All other actions are marked as SKIPPED</li>
     * </ul>
     */
    FAIL_FAST,

    /**
     * Similar to FAIL_FAST but attempts to restore the system to its previous state.
     *
     * <p>When a failure occurs:
     * <ul>
     *   <li>The deployment process halts immediately</li>
     *   <li>Initiates rollback of all successfully completed actions in reverse order</li>
     *   <li>Attempts to restore the system to its state before deployment started</li>
     *   <li>All other actions are marked as SKIPPED</li>
     * </ul>
     */
    ROLLBACK,

    /**
     * Continues the deployment process even when failures are encountered.
     *
     * <p>When a failure occurs:
     * <ul>
     *   <li>The failed action is marked as FAILED</li>
     *   <li>Dependent actions of the failed action are marked as SKIPPED</li>
     *   <li>Independent actions continue to execute normally</li>
     *   <li>The deployment process continues until all possible actions are processed</li>
     * </ul>
     */
    LAZY_FAILURE,
}
