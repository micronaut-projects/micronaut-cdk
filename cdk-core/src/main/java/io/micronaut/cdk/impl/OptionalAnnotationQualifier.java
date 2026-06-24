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
 * Qualifier that selects beans that satisfy the wrapped qualifier, or does not contain the qualifying annotation <b>at all</b>.
 * This qualifier ("Q") wraps another {@link Qualifier} ("Q2") based on an annotation A. Q accepts beans that are accepted by Q2
 * and beans that are not annotated by A at all. Beans that are not annotated by A are ordered last.
 *
 * @param <T> qualified bean type
 */
public final class OptionalAnnotationQualifier<T> extends FilteringQualifier<T> {
    private final Qualifier<T> qualifier;
    private final Class<? extends Annotation> qualifierClass;

    private OptionalAnnotationQualifier(Class<? extends Annotation> qualifierClass, Qualifier<T> qualifier) {
        this.qualifierClass = qualifierClass;
        this.qualifier = qualifier;
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
        return new OptionalAnnotationQualifier<>(qualifierClass, qualifier);
    }

    @Override
    public <BT extends BeanType<T>> Stream<BT> reduce(Class<T> beanType, Stream<BT> candidates) {
        Stream<BT> filtered = qualifier.reduce(beanType, candidates);
        return Stream.concat(filtered, candidates.filter(d -> d.getAnnotation(qualifierClass) == null)).distinct();
    }

    @Override
    public <BT extends BeanType<T>> Collection<BT> filter(Class<T> beanType, Collection<BT> candidates) {
        int size = candidates.size();
        if (size == 1) {
            return doesQualify(beanType, candidates.iterator().next()) ? candidates : List.of();
        }
        Collection<BT> col = qualifier.filter(beanType, candidates);
        boolean copied = false;
        for (BT def : candidates) {
            if (def.getAnnotation(qualifierClass) == null && !col.contains(def)) {
                if (!copied) {
                    col = new ArrayList<>(col);
                    copied = true;
                }
                col.add(def);
            }
        }
        return col;
    }

    @Override
    public boolean doesQualify(Class<T> beanType, BeanType<T> candidate) {
        AnnotationValue<? extends Annotation> val = candidate.getDeclaredAnnotation(qualifierClass);
        if (val == null) {
            return true;
        }
        return qualifier.doesQualify(beanType, candidate);
    }
}
