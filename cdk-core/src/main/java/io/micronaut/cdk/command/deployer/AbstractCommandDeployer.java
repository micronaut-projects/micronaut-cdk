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

import io.micronaut.cdk.ExecutionContext;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.command.CommandDeployer;
import io.micronaut.cdk.component.Component;
import io.micronaut.cdk.util.Assert;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static io.micronaut.cdk.ExecutionContext.UNKNOWN_VARIABLE;
import static java.util.Calendar.DAY_OF_MONTH;

/**
 * Base class for command deployers.
 * <p>
 * This class depends on an {@link ExecutionContext} to function correctly.
 * The {@code ExecutionContext} is injected automatically by the Micronaut dependency injection container.
 * </p>
 *
 * @param <S> spec type
 * @param <D> delete spec type
 * @param <N> cloud resource type
 * @param <C> cdk component type
 */
public abstract class AbstractCommandDeployer<S extends Spec<?>, D extends DeleteSpec<?>, N, C extends Component<N>> implements CommandDeployer<S, D, N, C> {

    /**
     * the execution context.
     */
    protected final ExecutionContext ec;

    /**
     * Constructor.
     *
     * @param ec the execution context
     */
    protected AbstractCommandDeployer(ExecutionContext ec) {
        this.ec = ec;
    }

    /**
     * Register a variable, storing UNKNOWN_VARIABLE if null.
     *
     * @param cdkId the CDK ID
     * @param name  the name
     * @param value the value
     */
    protected void registerVariable(@NonNull String cdkId,
                                    @NonNull String name,
                                    @Nullable String value) {
        Assert.hasText(cdkId, "cdkId is required");
        Assert.hasText(name, "name is required");

        ec.registerVariable(cdkId, name, value == null ? UNKNOWN_VARIABLE : value);
    }

    /**
     * Look for a component that's been resolved or deployed previously in the
     * current run to avoid unnecessary API call lookups.
     *
     * @param cdkId              the CDK ID
     * @param cloudComponentType the class of the cloud component in the <code>Component</code>
     * @param <C>                the type of the cloud component
     * @return the cloud component if found
     */
    @NonNull
    protected <C> Optional<C> findDeployed(@NonNull String cdkId,
                                           @NonNull Class<C> cloudComponentType) {
        Assert.hasText(cdkId, "cdkId is required");
        Assert.notNull(cloudComponentType, "cloudComponentType is required");

        return ec.findDeployed(cdkId, true, cloudComponentType);
    }

    /**
     * Create a date n days from now.
     *
     * @param days the number of days
     * @return the date
     */
    protected Date daysFromNow(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(DAY_OF_MONTH, days);
        return calendar.getTime();
    }

    /**
     * Return null if the list is null or empty.
     *
     * @param list the list
     * @param <T>  the list type
     * @return null if the list is empty, otherwise the list
     */
    protected <T> List<T> emptyListToNull(@Nullable List<T> list) {
        return list == null || list.isEmpty() ? null : list;
    }
}
