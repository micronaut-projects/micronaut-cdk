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
package io.micronaut.cdk.impl.processing;

import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.component.Component;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

/**
 * Information necessary to generate a delete builder factory call.
 *
 * @param factoryClass  factory class fqn
 * @param specClass     true, if the class is the Spec class, false for DeleteSpec class
 * @param factoryMethod the selected factory method element
 * @param kind          invocation kind - parameter style
 * @param returnType    return type of the method
 */
public record MethodInfo(String factoryClass, boolean specClass, ExecutableElement factoryMethod,
                         InvocationKind kind,
                         TypeElement returnType) implements Comparable<MethodInfo> {
    @Override
    public int compareTo(MethodInfo o) {
        if (kind != o.kind) {
            return kind.ordinal() - o.kind.ordinal();
        }
        if (specClass != o.specClass) {
            return specClass ? -1 : 1;
        }
        if (factoryMethod.getKind() == ElementKind.CONSTRUCTOR) {
            return o.factoryMethod.getKind() == ElementKind.CONSTRUCTOR ? 0 : 1;
        } else {
            return o.factoryMethod.getKind() != ElementKind.CONSTRUCTOR ? 0 : -1;
        }
    }

    /**
     * Name of the factory method.
     *
     * @return name of factory method
     */
    public String methodName() {
        return factoryMethod.getSimpleName().toString();
    }

    /**
     * True if the method uses generics.
     *
     * @return true to indicate generics
     */
    public boolean usesGenerics() {
        return factoryMethod.getTypeParameters().size() > 1;
    }

    /**
     * Determines the invocation style of the Builder method or constructor.
     */
    enum InvocationKind {
        /**
         * Not eligible as a Builder factory.
         */
        NONE,

        /**
         * Accepts both {@link Component} and {@link Spec}.
         */
        COMP_SPEC,

        /**
         * Accepts the resource Spec instance.
         */
        SPEC,

        /**
         * Accepts the Component instance.
         */
        COMP,

        /**
         * Accepts the CDK ID instance.
         */
        CDKID,
    }
}
