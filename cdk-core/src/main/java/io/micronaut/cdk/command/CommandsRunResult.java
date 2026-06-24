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

import java.util.List;
import java.util.Map;

/**
 * Encapsulates the result of executed commands.
 *
 * @param successfulCommands the list of commands that completed successfully
 * @param failedCommands     the list of commands that failed during execution
 * @param skippedCommands    the list of commands that were skipped and not executed
 * @param errors             a mapping of failed commands to the corresponding {@link Throwable} cause
 */
public record CommandsRunResult(List<Command> successfulCommands,
                                List<Command> failedCommands,
                                List<Command> skippedCommands,
                                Map<Command, Throwable> errors) {

    /**
     * @return true if any command failed during execution.
     */
    public boolean errorOccurred() {
        return !errors.isEmpty() || !failedCommands.isEmpty();
    }
}

