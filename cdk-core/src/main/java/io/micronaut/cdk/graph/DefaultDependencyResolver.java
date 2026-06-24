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
package io.micronaut.cdk.graph;

import io.micronaut.cdk.command.Command;
import io.micronaut.cdk.command.Commands;
import io.micronaut.cdk.util.Assert;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link DependencyResolver}.
 */
@Singleton
public class DefaultDependencyResolver implements DependencyResolver {

    DefaultDependencyResolver() {
    }

    @Override
    @NonNull
    public Commands sort(@NonNull Commands commands) throws DependencyCycleException {
        Assert.notNull(commands, "Commands cannot be null");

        var graph = new GraphBuilder(commands)
                .build();

        List<Command> sortedCommands = new ArrayList<>();

        List<Node> sortedNodes = graph.sort();
        for (var node : sortedNodes) {
            sortedCommands.add(node.getCommand());
        }

        return new Commands(sortedCommands);
    }
}
