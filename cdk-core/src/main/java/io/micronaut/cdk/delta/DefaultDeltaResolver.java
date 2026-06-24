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
package io.micronaut.cdk.delta;

import io.micronaut.cdk.ExecutionContext;
import io.micronaut.cdk.action.Action;
import io.micronaut.cdk.action.Executable;
import io.micronaut.cdk.action.GroupAction;
import io.micronaut.cdk.action.QualifiedServiceProvider;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.component.Component;
import io.micronaut.cdk.component.ExistingComponents;
import io.micronaut.cdk.util.Assert;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link DeltaResolver}.
 */
@Singleton
public class DefaultDeltaResolver implements DeltaResolver {

    private final QualifiedServiceProvider serviceProvider;

    private final ExecutionContext ec;

    DefaultDeltaResolver(QualifiedServiceProvider serviceProvider,
                         ExecutionContext ec) {
        this.serviceProvider = serviceProvider;
        this.ec = ec;
    }

    /**
     * Builds a {@link Delta} and sets it in the context.
     *
     * @throws DeltaResolutionException if there's a problem
     */
    @Override
    public void resolve() throws DeltaResolutionException {
        Assert.notNull(ec, "ExecutionContext cannot be null");

        Collection<Action<?>> create = new ArrayList<>();
        Map<DeleteSpec<?>, Component<?>> delete = new HashMap<>();
        Collection<Action<?>> execute = new ArrayList<>();
        Collection<Action<?>> noop = new ArrayList<>();
        Collection<Action<?>> unspecified = new ArrayList<>();
        Map<Action<?>, Component<?>> update = new HashMap<>();

        for (Action<?> action : ec.getActions()) {
            if (action instanceof Executable) {
                execute.add(action);
            } else {
                Component<?> component;
                if (action instanceof DeleteSpec<?> deleteSpec) {
                    if (deleteSpec.isByCdkId()) {
                        component = findByCdkId(deleteSpec.getId(), ec.getExistingComponents());
                    } else {
                        component = findByCloudId(deleteSpec.getId(), ec.getExistingComponents());
                    }
                    delete.put(deleteSpec, component);
                } else if (action instanceof Spec) {
                    component = findByCdkId(action.getCdkId(), ec.getExistingComponents());
                    if (component == null) {
                        create.add(action);
                    } else {
                        boolean needsUpdate = false;
                        Collection<UpdateResolver> resolvers = serviceProvider.findServices(UpdateResolver.class, action);
                        for (var updateResolver : resolvers) {
                            if (updateResolver.needsUpdate((Spec<?>) action, component)) {
                                update.put(action, component);
                                needsUpdate = true;
                                break;
                            }
                        }
                        if (!needsUpdate) {
                            noop.add(action);
                        }
                    }
                } else if (action instanceof GroupAction) {
                    unspecified.add(action);
                }
            }
        }

        ec.setDelta(new Delta(create, delete, update, noop, execute, unspecified));
    }

    private Component<?> findByCdkId(@NonNull String cdkId,
                                     @NonNull ExistingComponents existingComponents) {

        return existingComponents
                .stream()
                .filter(it -> cdkId.equals(it.getCdkId()))
                .findFirst()
                .orElse(null);
    }

    private Component<?> findByCloudId(@NonNull String cloudId,
                                       @NonNull ExistingComponents existingComponents) {

        return existingComponents
                .stream()
                .filter(it -> cloudId.equals(it.getCloudId()))
                .findFirst()
                .orElse(null);
    }
}
