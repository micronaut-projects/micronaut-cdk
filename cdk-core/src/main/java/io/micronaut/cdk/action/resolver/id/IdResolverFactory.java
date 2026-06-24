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
package io.micronaut.cdk.action.resolver.id;

import io.micronaut.cdk.CdkConfigurationProperties;
import io.micronaut.cdk.action.IdResolver;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.inject.Singleton;

import java.lang.reflect.InvocationTargetException;

@Factory
class IdResolverFactory {

    private final CdkConfigurationProperties cdkConfigurationProperties;
    private final ApplicationContext ctx;

    IdResolverFactory(CdkConfigurationProperties cdkConfigurationProperties,
                      ApplicationContext ctx) {
        this.cdkConfigurationProperties = cdkConfigurationProperties;
        this.ctx = ctx;
    }

    @Singleton
    @Primary
    IdResolver idResolver() {
        if (cdkConfigurationProperties.getIdresolver().getClassName() != null) {
            return createResolver(cdkConfigurationProperties.getIdresolver().getClassName());
        }

        return ctx.getBean(IdResolver.class, Qualifiers.byName(cdkConfigurationProperties.getIdresolver().getName().name().toLowerCase()));
    }

    private IdResolver createResolver(String className) {
        try {
            Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(className);
            return (IdResolver) c.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                 IllegalAccessException | NoSuchMethodException e) {
            throw new IllegalStateException("Unable to instantiate IdResolver from class '" + className + "'", e);
        }
    }
}
