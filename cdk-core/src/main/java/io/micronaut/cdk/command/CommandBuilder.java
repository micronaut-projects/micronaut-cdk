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

import io.micronaut.cdk.delta.Delta;
import io.micronaut.context.annotation.DefaultImplementation;

/**
 * Builds commands to run based on work specified in a {@link Delta}.
 */
@FunctionalInterface
@DefaultImplementation(DefaultCommandBuilder.class)
public interface CommandBuilder {

    /**
     * Builds a {@link Commands} and sets it in the context.
     *
     * @throws CommandBuildException if there's a problem
     */
    void build() throws CommandBuildException;
}
