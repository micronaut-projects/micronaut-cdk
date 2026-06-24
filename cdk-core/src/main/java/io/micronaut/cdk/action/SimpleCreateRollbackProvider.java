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

import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.annotations.ComponentService;
import io.micronaut.cdk.command.ActionService;
import io.micronaut.cdk.command.Command;
import io.micronaut.cdk.component.Component;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Simple implementation that handles {@link Spec} actions and creates {@link DeleteSpec} compensations
 * using {@link ResourceSpecConverter#deleteComponent(Component, Spec)}. Should serve as a default fallback
 * for all Components.
 *
 * @param <A> spec action type
 */
@Singleton
@ComponentService(Component.class)
@ActionService(Spec.class)
public class SimpleCreateRollbackProvider<A extends Spec<A>> implements RollbackProvider<Object, Component<Object>, A> {
    private final ResourceSpecConverter converter;

    public SimpleCreateRollbackProvider(ResourceSpecConverter converter) {
        this.converter = converter;
    }

    @Override
    public List<Action<?>> createRollback(Command command, A action, CommandResult<Object, Component<Object>> result) {
        if (command.isUpdate()) {
            return null;
        }
        if (result.component() != null) {
            @SuppressWarnings({"rawtypes", "unchecked"})
            DeleteSpec.Builder<?, ?> builder = converter.deleteComponent(result.component(), (Spec) command.getSpec());
            if (builder != null) {
                builder.withComponentClass(result.component().getClass());
                return List.of(builder.build());
            }
        }
        if (result.component() == null) {
            // probably the custom factory cannot work with Spec, and the Component is already gone.
            // create a fake Group action.
            return List.of(GroupAction.builder(action.getCdkId())
                    .label("Placeholder for missing component " + action.getCdkId())
                    .internal(true)
                    .build()
            );
        }
        return null;
    }
}
