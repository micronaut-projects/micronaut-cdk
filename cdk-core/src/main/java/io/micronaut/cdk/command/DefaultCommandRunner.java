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

import io.micronaut.cdk.CdkConfigurationProperties;
import io.micronaut.cdk.ExecutionContext;
import io.micronaut.cdk.graph.Graph;
import io.micronaut.cdk.graph.GraphBuilder;
import io.micronaut.cdk.graph.GraphExecutor;
import io.micronaut.cdk.util.Assert;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Long.MAX_VALUE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Default implementation of {@link CommandRunner}.
 */
@Singleton
public class DefaultCommandRunner implements CommandRunner {

    private final TaskFactory<Command> factory;
    private final CdkConfigurationProperties configuration;
    private final ExecutionContext ec;

    DefaultCommandRunner(TaskFactory<Command> factory,
                         CdkConfigurationProperties configuration,
                         ExecutionContext ec) {
        this.factory = factory;
        this.configuration = configuration;
        this.ec = ec;
    }

    @Override
    public CommandsRunResult run() throws CommandRunException {
        Assert.notNull(ec, "ExecutionContext cannot be null");

        for (var command : ec.getCommands()) {
            if (command.isUpdate()) {
                throw new IllegalStateException("update is not supported yet");
            }
        }

        if (ec.isDryRun()) {
            // No commands executed, return empty result.
            return new CommandsRunResult(List.of(), List.of(), List.of(), Map.of());
        }

        Graph graph = new GraphBuilder(ec.getCommands()).build();

        ExecutorService executor = Executors.newFixedThreadPool(
                configuration.getThreads(), new CdkThreadFactory());

        new GraphExecutor(graph, executor, ec, factory).run();
        awaitTerminationAndShutdown(executor);

        return ec.collectRunResults();
    }

    private void awaitTerminationAndShutdown(ExecutorService executor) {
        try {
            if (!executor.awaitTermination(MAX_VALUE, MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
