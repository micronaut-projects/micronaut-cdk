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
package io.micronaut.cdk.action;

import io.micronaut.cdk.ExecutionContext;
import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.annotations.ResourceSpec;
import io.micronaut.cdk.component.Component;
import io.micronaut.cdk.component.ExistingComponents;
import io.micronaut.cdk.impl.ComponentAccessor;
import io.micronaut.cdk.util.Assert;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * Default implementation of {@link ExistingComponentResolver}. It first tries to
 * resolve components using ExecutionContext's cache, then queries the cloud/component
 * services.
 */
@Singleton
public class DefaultExistingComponentResolver implements ExistingComponentResolver {

    private final QualifiedServiceProvider serviceProvider;
    private final IdResolver idResolver;
    private final ExecutionContext ec;

    DefaultExistingComponentResolver(QualifiedServiceProvider serviceProvider,
                                     IdResolver idResolver,
                                     ExecutionContext ec) {
        this.serviceProvider = serviceProvider;
        this.idResolver = idResolver;
        this.ec = ec;
    }

    /**
     * Resolves {@link ExistingComponents}. It first attempts to locate the Action's Component
     * among the already deployed ones.
     *
     * @param actions actions to resolve.
     * @return populated existing components.
     */
    @Override
    public ExistingComponents resolve(@NonNull Iterable<Action<?>> actions) throws ComponentResolverException {
        Assert.notNull(ec, "ExecutionContext cannot be null");

        Collection<Component<?>> existing = new ArrayList<>();
        for (Action<?> action : actions) {
            Optional<Component<?>> c = findDeployed(action, ec).or(() -> findExisting(action));
            if (c.isPresent()) {
                Component<?> component = c.get();
                if (component.getCloud() == null) {
                    ComponentAccessor.getInstance().setCloud(component, ec.getDefaultCloud());
                }
                existing.add(component);
            }
        }

        return new ExistingComponents(existing);
    }

    /**
     * Finds known deployed component that is to be worked on by the action. The default implementation supports
     * {@link DeleteSpec} and {@link Spec} style actions.
     * <p>
     * NOTE: this implementation relaxes type safety and is an interim solution before
     * {@link ResourceSpec} annotation is utilized to return
     *
     * @param action action whose Component should be resolved
     * @param ec     execution context
     * @return Non-empty Optional, if a deployed Component matching the action is known.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    Optional<Component<?>> findDeployed(@NonNull Action<?> action, ExecutionContext ec) {
        if (action instanceof DeleteSpec<?> del) {
            return (Optional) ec.findDeployed(del.getId(), del.isByCdkId(), Component.class);
        } else if (action instanceof Spec) {
            return (Optional) ec.findDeployed(action.getCdkId(), true, Component.class);
        } else {
            return Optional.empty();
        }
    }

    @Override
    @NonNull
    public Optional<Component<?>> findExisting(@NonNull Action<?> action) {
        Assert.notNull(action, "Action cannot be null");
        Assert.notNull(ec, "ExecutionContext cannot be null");

        @SuppressWarnings("rawtypes")
        Collection<ComponentResolver> componentResolvers = serviceProvider.findServices(ComponentResolver.class, action);

        for (var resolver : componentResolvers) {
            @SuppressWarnings("unchecked")
            Optional<Component<?>> optional = resolver.findExisting(action);
            if (optional.isPresent()) {
                var component = optional.get();
                ec.registerResolved(component);
                idResolver.associate(component.getCdkId(), component.getCloudId());
                return optional;
            }
        }

        return Optional.empty();
    }
}
