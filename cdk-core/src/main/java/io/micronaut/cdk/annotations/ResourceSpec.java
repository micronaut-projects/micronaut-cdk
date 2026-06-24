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

import io.micronaut.cdk.action.DeleteComponentBuilderFactory;
import io.micronaut.cdk.action.ResourceSpecConverter;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.component.Component;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a Resource specification. Should be used on a *Spec subclass only. Associates the {@link Spec} subclass
 * to a specific {@link Component} that wraps the cloud resource data. It also associates a {@link DeleteSpec}
 * class for deleting the resource type. The {@link #component()} is mandatory. If {@link #deleteSpec()} is not specified,
 * the associated DeleteSpec subclass is guessed. If the Spec class itself contains {@code deleteBuilder()} methods that accept
 * <ul>
 *     <li>the annotated Spec class itself,</li>
 *     <li>{@link Component} specified by this annotation</li>
 *     <li>(Component, Spec)</li>
 *     <li>(String id, boolean byCdkId)</li>
 * </ul>
 * and method's return type is the {@link DeleteSpec.Builder} subtype, that Builder's enclosing DeleteSpec subtype is guessed
 * as the DeleteSpec for this Spec and Component.
 * <p>
 * If no such method is found, the processor attempts to search by naming convention:
 * <ol>
 *     <li>If the spec class is (cloud)(resource)Spec, the basename for the DeleteSpec will be (resource)DeleteSpec. The following classes
 *     are tried:</li>
 *     <li>cdk.action.delete.(cloud)(basename)</li>
 *     <li>cdk.action.delete.(cloud).(basename)</li>
 *     <li>cdk.action.delete.(basename)</li>
 * </ol>
 * A concrete (non-abstract) resource Spec class must be also annotated with {@link CloudSpecific} with the
 * appropriate cloud as a value. If all conditions are satisfied, a {@link DeleteComponentBuilderFactory} will be generated for the
 * specified Cloud and a resource spec type, so that the default implementation of {@link ResourceSpecConverter} will locate
 * it. The generation can be suppressed by setting {@link #generateDeleteFactory()} to {@code false}.
 * <p>
 * The code generator searches in the annotated {@code Spec} class and in the specified or guessed {@code DeleteSpec} class for
 * factory methods that return a DeleteSpec.Builder using the patterns above.
 * The DeleteSpec class is searched for methods:
 * <ol>
 *     <li>builder(Component comp, Spec spec)</li>
 *     <li>builder(Spec spec)</li>
 *     <li>builder(Component comp)</li>
 *     <li>builder(String id, boolean byCdkId)</li>
 * </ol>
 * The Spec is searched for {@code deleteBuilder} methods with the same signature. Spec's factory methods are preferred.
 * The implementation is generated as follows:
 * <ol>
 *     <li>If {@code builder(Component comp, Spec spec)} is present, the generated {@link DeleteComponentBuilderFactory}
 *          will blindly delegate to this factory, leaving all logic to its implementation</li>
 *     <li>
 *        If {@code builder(Spec)} exists, the generated code will delegate to it, if its
 *        {@link DeleteComponentBuilderFactory#deleteBuilder(Component, Spec)} gets non-null {@code spec} parameter
 *     </li>
 *     <li>
 *         If {@code builder(Component)} exists, it is used.
 *     </li>
 *     <li>
 *         The {@code builder(String, boolean)} is used, passing CDK ID or Cloud ID depending on runtime parameters.
 *     </li>
 * </ol>
 */
@Retention(RUNTIME)
@Inherited
public @interface ResourceSpec {

    /**
     * Specifies the Component subclass this specification stands for.
     *
     * @return component type
     */
    @SuppressWarnings("rawtypes")
    Class<? extends Component> component();

    /**
     * Specifies the DeleteSpec class to be used.
     *
     * @return the matching DeleteSpec class.
     */
    Class<? extends DeleteSpec> deleteSpec() default DeleteSpec.class;

    /**
     * False suppresses generating {@link DeleteComponentBuilderFactory}. The default
     * is {@code true}.
     *
     * @return true (default) to generate delete factory implementation.
     */
    boolean generateDeleteFactory() default true;
}
