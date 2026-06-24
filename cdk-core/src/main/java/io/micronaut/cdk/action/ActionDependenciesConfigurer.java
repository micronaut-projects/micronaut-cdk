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

import io.micronaut.cdk.component.ComponentReference;
import io.micronaut.core.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configures dependencies inferred from component references embedded in specs.
 */
final class ActionDependenciesConfigurer {

    void configure(@NonNull Iterable<Action<?>> actions) {
        Map<String, Action<?>> actionsByCdkId = new LinkedHashMap<>();

        for (Action<?> action : actions) {
            actionsByCdkId.putIfAbsent(action.getCdkId(), action);
        }

        for (Action<?> action : actions) {
            if (!(action instanceof Spec<?> spec)) {
                continue;
            }

            for (ComponentReference reference : findReferences(spec)) {
                String referencedCdkId = reference.getCdkId();
                if (referencedCdkId == null) {
                    continue;
                }

                Action<?> dependency = actionsByCdkId.get(referencedCdkId);
                if (dependency != null && dependency != action && !action.getDependencies().contains(dependency)) {
                    action.addDependency(dependency);
                }
            }
        }
    }

    @NonNull
    private Collection<ComponentReference> findReferences(@NonNull Spec<?> spec) {
        Collection<ComponentReference> references = new ArrayList<>();

        for (Method method : spec.getClass().getMethods()) {
            if (method.getParameterCount() != 0 || method.getDeclaringClass() == Object.class) {
                continue;
            }

            try {
                Object value = method.invoke(spec);
                if (value instanceof ComponentReference reference) {
                    references.add(reference);
                } else if (value instanceof Iterable<?> iterable) {
                    for (Object item : iterable) {
                        if (item instanceof ComponentReference reference) {
                            references.add(reference);
                        }
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Failed to inspect references for spec " + spec.getClass().getName(), e);
            }
        }

        return references;
    }
}
