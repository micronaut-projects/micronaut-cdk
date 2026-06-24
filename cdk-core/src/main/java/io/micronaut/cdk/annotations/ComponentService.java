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
package io.micronaut.cdk.annotations;

import io.micronaut.cdk.command.CommandDeployer;
import io.micronaut.cdk.component.Component;
import jakarta.inject.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks the annotated bean as a collaborator of a certain {@link Component} resource.
 * Use with services like {@link CommandDeployer} that operate only with certain
 * component types.
 */
@Qualifier
@Inherited
@Documented
@Retention(RUNTIME)
public @interface ComponentService {

    /**
     * Specifies the Component subclass this class operates with.
     *
     * @return Component subclass
     */
    @SuppressWarnings("rawtypes")
    Class<? extends Component> value();
}
