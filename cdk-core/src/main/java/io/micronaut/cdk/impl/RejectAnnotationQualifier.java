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

import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.inject.BeanType;
import io.micronaut.inject.qualifiers.FilteringQualifier;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Qualifier that rejects beans that use a certain qualifier, possibly a certain qualifier value.
 *
 * @param <T> qualified bean type
 */
public final class RejectAnnotationQualifier<T> extends FilteringQualifier<T> {
    private final Class<? extends Annotation> qualifierClass;
    private final Qualifier<T> rejectValue;

    private RejectAnnotationQualifier(Class<? extends Annotation> qualifierClass, Qualifier<T> rejectValue) {
        this.qualifierClass = qualifierClass;
        this.rejectValue = rejectValue;
    }

    /**
     * Selects beans that completely lack the annotation.
     *
     * @param qualifierClass annotation type
     * @param <T>            qualified bean type
     * @return qualifier instance
     */
    public static <T> Qualifier<T> reject(Class<? extends Annotation> qualifierClass) {
        return new RejectAnnotationQualifier<>(qualifierClass, null);
    }

    /**
     * Selects beans that completely lack the annotation, or the annotation specifies a different
     * value, NOT accepted by the qualifier.
     *
     * @param qualifierClass annotation type
     * @param rejectValue    qualifier that accepts the rejected value
     * @param <T>            qualified bean type
     * @return qualifier instance
     */
    public static <T> Qualifier<T> reject(Class<? extends Annotation> qualifierClass, Qualifier<T> rejectValue) {
        return new RejectAnnotationQualifier<>(qualifierClass, rejectValue);
    }

    @Override
    public <BT extends BeanType<T>> Stream<BT> reduce(Class<T> beanType, Stream<BT> candidates) {
        return candidates.filter(d -> d.getAnnotation(qualifierClass) == null ||
                (rejectValue != null && !rejectValue.doesQualify(beanType, d)));
    }

    @Override
    public <BT extends BeanType<T>> Collection<BT> filter(Class<T> beanType, Collection<BT> candidates) {
        int size = candidates.size();
        if (size == 1) {
            return doesQualify(beanType, candidates.iterator().next()) ? candidates : List.of();
        }
        Collection<BT> col = new ArrayList<>();
        for (BT def : candidates) {
            if ((def.getAnnotation(qualifierClass) == null ||
                    (rejectValue != null && !rejectValue.doesQualify(beanType, def))
            ) && !col.contains(def)) {
                col.add(def);
            }
        }
        return col;
    }

    @Override
    public boolean doesQualify(Class<T> beanType, BeanType<T> candidate) {
        AnnotationValue<? extends Annotation> val = candidate.getDeclaredAnnotation(qualifierClass);
        return val == null || (rejectValue != null && !rejectValue.doesQualify(beanType, candidate));
    }
}
