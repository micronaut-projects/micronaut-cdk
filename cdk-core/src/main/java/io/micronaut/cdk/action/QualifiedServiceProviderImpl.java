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
import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ComponentService;
import io.micronaut.cdk.component.Component;
import io.micronaut.cdk.impl.CdkQualifiers;
import io.micronaut.context.BeanContext;
import io.micronaut.context.Qualifier;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * Implementation of {@link QualifiedServiceProvider}.
 */
@Singleton
public class QualifiedServiceProviderImpl implements QualifiedServiceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(QualifiedServiceProviderImpl.class);

    private final BeanContext beanContext;
    private final ExecutionContext executionContext;

    private final ResourceSpecConverter converter;

    public QualifiedServiceProviderImpl(BeanContext context, ExecutionContext ec, ResourceSpecConverter converter) {
        this.beanContext = context;
        this.executionContext = ec;
        this.converter = converter;
    }

    @Override
    public <T> Collection<T> findServices(Class<T> beanType, Action<?> action) {
        Qualifier<T> qual;
        Class<?> componentClass = null;
        if (action instanceof Spec<?> spec) {
            try {
                componentClass = converter.findComponentClass(spec);
            } catch (UnsupportedOperationException ex) {
                // expected, for transition of clouds
                LOG.warn("No component class found for spec {}", spec);
            }
        } else if (action instanceof DeleteSpec<?> deleteSpec) {
            try {
                componentClass = converter.findComponentClass(deleteSpec);
            } catch (UnsupportedOperationException ex) {
                // expected, for transition of clouds
                LOG.warn("No component class found for deleteSpec {}", deleteSpec);
            }
        } else {
            // do not throw for Exec action or Group actions
            return List.of();
        }
        if (componentClass == null) {
            componentClass = Component.class;
        }
        qual = Qualifiers.byQualifiers(
                // also accept implementations without qualifier, for transition of old code.
                CdkQualifiers.preferAcceptsType(ComponentService.class, componentClass),
                CdkQualifiers.byAnnotationValue(CloudSpecific.class, executionContext.getDefaultCloud())
        );
        return beanContext.getBeansOfType(beanType, qual);
    }
}
