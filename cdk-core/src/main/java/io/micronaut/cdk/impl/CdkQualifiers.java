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
package io.micronaut.cdk.impl;

import io.micronaut.cdk.action.Action;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.action.spec.ProjectSpec;
import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.inject.annotation.MutableAnnotationMetadata;
import io.micronaut.inject.qualifiers.Qualifiers;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * This class allows to construct special Qualifiers to search Micronaut's {@link io.micronaut.context.BeanContext}
 * for component/action specific services.
 */
public final class CdkQualifiers {
    private CdkQualifiers() {
    }

    /**
     * Creates a Qualifier matching a certain value of the specified annotation.
     *
     * @param annotation the annotation type
     * @param value      the value.
     * @param <T>        type of the qualified bean.
     * @return Qualifier instance
     */
    public static <T> Qualifier<T> byAnnotationValue(Class<? extends Annotation> annotation, Class<?> value) {
        MutableAnnotationMetadata md1 = new MutableAnnotationMetadata();
        md1.addAnnotation(annotation.getName(), Map.of("value",
                new AnnotationClassValue<>(value)));
        return Qualifiers.byAnnotation(md1, annotation);
    }

    /**
     * Creates a Qualifier matching a certain value of the specified annotation.
     *
     * @param annotation the annotation type
     * @param value      the value.
     * @param <T>        type of the qualified bean.
     * @param <V>        value type.
     * @return Qualifier instance
     */
    public static <T, V> Qualifier<T> byAnnotationValue(Class<? extends Annotation> annotation, V value) {
        MutableAnnotationMetadata md1 = new MutableAnnotationMetadata();
        md1.addAnnotation(CloudSpecific.class.getName(),
                value instanceof Enum<?> e ?
                        Map.of("value", e.name()) :
                        Map.of("value", value)
        );
        return Qualifiers.byAnnotation(md1, annotation);
    }

    /**
     * Creates a qualifier selecting classes annotated with a certain type or its supertypes. The qualifier
     * accepts beans annotated with the given type or one of its supertypes. It orders selected beans so that
     * types that accept the nearest supertype come first.
     *
     * @param annotation    the annotation searched for
     * @param attributeName member of the annotation, that contains class value
     * @param type          the type to search for.
     * @param <T>           qualified bean type
     * @return qualifier instance
     */
    public static <T> Qualifier<T> acceptsType(Class<? extends Annotation> annotation, String attributeName, Class<?> type) {
        return MatchesTypeQualifier.acceptsType(annotation, attributeName, type);
    }

    /**
     * Creates a qualifier selecting classes annotated with a certain type or its supertypes. The qualifier
     * accepts beans annotated with the given type or one of its supertypes. It orders selected beans so that
     * types that accept the nearest supertype come first. This method always uses the default ("value") annotation member.
     *
     * @param annotation the annotation searched for
     * @param type       the type to search for.
     * @param <T>        qualified bean type
     * @return qualifier instance
     */
    public static <T> Qualifier<T> acceptsType(Class<? extends Annotation> annotation, Class<?> type) {
        return acceptsType(annotation, "value", type);
    }

    /**
     * Creates a qualifier selecting classes annotated with a certain type or its supertypes. The qualifier
     * accepts beans annotated with the given type or one of its supertypes, but beans that accept types
     * that are not most specific from the available bean types will be ignored. E.g. if there's a bean
     * A accepting a {@link Spec}, bean B accepting an {@link Action} and two beans (C,D)
     * accepting {@link ProjectSpec}, then
     * <ul>
     *     <li>C,D will be returned for ProjectSpec</li>
     *     <li>A will be returned for Spec or any other Spec subclass except ProjectSpec</li>
     *     <li>B will be returned for Action, or DeleteSpec</li>
     * </ul>
     *
     * @param annotation    the annotation searched for
     * @param attributeName member of the annotation, that contains class value
     * @param type          the type to search for.
     * @param <T>           qualified bean type
     * @return qualifier instance
     */
    public static <T> Qualifier<T> closestType(Class<? extends Annotation> annotation, String attributeName, Class<?> type) {
        return MatchesTypeQualifier.closestType(annotation, attributeName, type);
    }

    /**
     * Creates a qualifier selecting classes annotated with a certain type or its supertypes. The qualifier
     * accepts beans annotated with the given type or one of its supertypes, but beans that accept types
     * that are not most specific from the available bean types will be ignored.
     * For detailed description, see {@link #closestType(Class, String, Class)}
     *
     * @param annotation the annotation searched for
     * @param type       the type to search for.
     * @param <T>        qualified bean type
     * @return qualifier instance
     * @see #closestType(Class, String, Class)
     */
    public static <T> Qualifier<T> closestType(Class<? extends Annotation> annotation, Class<?> type) {
        return closestType(annotation, "value", type);
    }

    /**
     * Creates a qualifier selecting classes annotated with a certain type or its supertypes. The qualifier
     * accepts beans annotated with the given type or one of its supertypes. It orders selected beans so that
     * types that accept the nearest supertype come first. Beans that does not contain the qualifier {@code annotation} at all
     * are also included last.
     *
     * @param annotation    the annotation searched for
     * @param attributeName member of the annotation, that contains class value
     * @param type          the type to search for.
     * @param <T>           qualified bean type
     * @return qualifier instance
     */
    public static <T> Qualifier<T> preferAcceptsType(Class<? extends Annotation> annotation, String attributeName, Class<?> type) {
        return MatchesTypeQualifier.preferAcceptsType(annotation, attributeName, type);
    }

    /**
     * Creates a qualifier selecting classes annotated with a certain type or its supertypes. The qualifier
     * accepts beans annotated with the given type or one of its supertypes. It orders selected beans so that
     * types that accept the nearest supertype come first. Beans that does not contain the qualifier {@code annotation} at all
     * are also included last. Always uses the default ("value") annotation member.
     *
     * @param annotation the annotation searched for
     * @param type       the type to search for.
     * @param <T>        qualified bean type
     * @return qualifier instance
     */
    public static <T> Qualifier<T> preferAcceptsType(Class<? extends Annotation> annotation, Class<?> type) {
        return preferAcceptsType(annotation, "value", type);
    }

    /**
     * Optionally selects beans that satisfy the given qualifier, or completely lack the qualifying annotation.
     *
     * @param qualifierClass annotation type
     * @param qualifier      the selecting qualifier
     * @param <T>            qualified bean type
     * @return qualifier instance
     */
    public static <T> Qualifier<T> optionally(Class<? extends Annotation> qualifierClass, Qualifier<T> qualifier) {
        return OptionalAnnotationQualifier.optionally(qualifierClass, qualifier);
    }

    /**
     * Creates a {@link Qualifier} that selects beans that do not use the specified qualifier annotation at all.
     *
     * @param rejectQualifier class of qualifier that must not be used by a bean.
     * @param <T>             bean type.
     * @return qualifier instance
     */
    public static <T> Qualifier<T> reject(Class<? extends Annotation> rejectQualifier) {
        return RejectAnnotationQualifier.reject(rejectQualifier);
    }

    /**
     * Creates a {@link Qualifier} that selects beans that do not use the specified qualifier annotation at all, or
     * uses a value DIFFERENT from the specified one. You must specify same qualifier class is used by the
     * {@code rejectQualifier} internally.
     *
     * @param rejectQualifier class of qualifier that must not be used by a bean.
     * @param value           value to be rejected
     * @param <T>             bean type.
     * @param <V>             value type
     * @return qualifier instance
     */
    public static <T, V> Qualifier<T> reject(Class<? extends Annotation> rejectQualifier, V value) {
        return RejectAnnotationQualifier.reject(rejectQualifier,
                byAnnotationValue(rejectQualifier, value)
        );
    }
}
