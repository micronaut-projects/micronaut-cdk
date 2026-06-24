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

import io.micronaut.cdk.ExecutionContext;
import io.micronaut.cdk.action.IdResolver;
import io.micronaut.cdk.action.QualifiedServiceProvider;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;

import java.util.concurrent.Callable;

/**
 * Creates run command tasks.
 */
@Singleton
public class RunCommandTaskFactory implements TaskFactory<Command> {

    private final ExecutableRunner executableRunner;
    private final QualifiedServiceProvider qualifiedServiceProvider;
    private final IdResolver idResolver;
    private final ExecutionContext ec;

    RunCommandTaskFactory(ExecutableRunner executableRunner,
                          QualifiedServiceProvider qualifiedServiceProvider,
                          IdResolver idResolver,
                          ExecutionContext ec) {
        this.idResolver = idResolver;
        this.executableRunner = executableRunner;
        this.qualifiedServiceProvider = qualifiedServiceProvider;
        this.ec = ec;
    }

    @Override
    public Callable<Command> createTask(@NonNull Command command) {
        return new RunCommandTask(ec, command,
                () -> qualifiedServiceProvider.findServices(CommandDeployer.class, command.getAction()),
                executableRunner, idResolver);
    }
}
