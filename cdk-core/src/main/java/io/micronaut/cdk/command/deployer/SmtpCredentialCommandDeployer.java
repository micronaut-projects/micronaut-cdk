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
package io.micronaut.cdk.command.deployer;

import io.micronaut.cdk.action.delete.SmtpCredentialDeleteSpec;
import io.micronaut.cdk.action.spec.SmtpCredentialSpec;
import io.micronaut.cdk.command.Command;
import io.micronaut.cdk.command.CommandDeployer;
import io.micronaut.cdk.command.CommandRunException;
import io.micronaut.cdk.component.Component;
import io.micronaut.cdk.component.SmtpCredential;
import io.micronaut.cdk.util.Assert;
import io.micronaut.core.annotation.NonNull;

/**
 * Deployer for {@link SmtpCredentialSpec}.
 */
public interface SmtpCredentialCommandDeployer extends CommandDeployer {

    default Component<?> deploy(@NonNull Command command) throws CommandRunException {
        Assert.notNull(command, "Command cannot be null");

        if (command.isUpdate() || !(command.getSpec() instanceof SmtpCredentialSpec)) {
            return null;
        }

        return deploySmtpCredential(command);
    }

    default boolean undeploy(@NonNull Command command) throws CommandRunException {
        Assert.notNull(command, "Command cannot be null");

        if (!command.isDelete() || !(command.getDeleteSpec() instanceof SmtpCredentialDeleteSpec)) {
            return false;
        }

        return undeploySmtpCredential(command);
    }

    /**
     * Deploy.
     *
     * @param command the command
     * @return the deployed instance
     * @throws CommandRunException if there's a problem
     */
    @NonNull
    SmtpCredential<?> deploySmtpCredential(@NonNull Command command) throws CommandRunException;

    /**
     * Undeploy.
     *
     * @param command the command
     * @return true if undeployed
     * @throws CommandRunException if there's a problem
     */
    boolean undeploySmtpCredential(@NonNull Command command) throws CommandRunException;
}
