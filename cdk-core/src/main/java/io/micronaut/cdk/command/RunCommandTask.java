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
import io.micronaut.cdk.action.Executable;
import io.micronaut.cdk.action.IdResolver;
import io.micronaut.cdk.command.Command.Status;
import io.micronaut.cdk.component.Component;
import io.micronaut.core.annotation.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Run command Task.
 */
class RunCommandTask implements Callable<Command> {

    private final ExecutionContext ec;
    private final Command command;
    private final Supplier<Collection<CommandDeployer>> deployers;
    private final ExecutableRunner executableRunner;
    private final IdResolver idResolver;

    RunCommandTask(@NonNull ExecutionContext ec,
                   @NonNull Command command,
                   @NonNull Supplier<Collection<CommandDeployer>> deployers,
                   @NonNull ExecutableRunner executableRunner,
                   @NonNull IdResolver idResolver) {
        this.ec = ec;
        this.command = command;
        this.deployers = deployers;
        this.executableRunner = executableRunner;
        this.idResolver = idResolver;
    }

    @Override
    public Command call() {
        command.setCommandStatus(Status.RUNNING);
        if (command.isExecutable()) {
            List<Executable.Result> results = executableRunner.run(command.getExecutable());
            ec.registerExecuted(command, results);
        } else if (command.isDelete()) {
            boolean undeployed = undeploy(command);
            if (undeployed) {
                ec.registerUndeployed(command.getDeleteSpec());
            } else {
                throw new UnsupportedOperationException("Cannot undeploy spec " + command.getDeleteSpec());
            }
        } else if (command.isCreate()) {
            Component<?> deployed = deploy(command);
            if (deployed != null) {
                ec.registerDeployed(command.getSpec(), deployed);
            } else {
                throw new UnsupportedOperationException("Cannot deploy spec " + command.getSpec());
            }
        }
        return command;
    }

    private Component<?> deploy(@NonNull Command command) {
        for (var deployer : deployers.get()) {
            Component<?> deployed = deployer.deploy(command, command.getSpec());
            if (deployed != null) {
                idResolver.associate(deployed.getCdkId(), deployed.getCloudId());
                return deployed;
            }
        }
        return null;
    }

    private boolean undeploy(@NonNull Command command) {
        for (var deployer : deployers.get()) {
            if (deployer.undeploy(command, command.getDeleteSpec())) {
                return true;
            }
        }
        return false;
    }
}
