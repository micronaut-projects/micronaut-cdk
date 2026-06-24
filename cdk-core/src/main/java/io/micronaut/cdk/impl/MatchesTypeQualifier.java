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
import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.util.StreamUtils;
import io.micronaut.inject.BeanType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This Qualifier not only filters beans for superclasses of the qualifier's class, but also orders the
 * matching beans so that beans that match the nearest superclasses come first. This allows for implicit order
 * with 'most suitable' beans coming first. Beans can be explicitly sorted using {@link io.micronaut.core.order.Ordered} though.
 *
 * @param <T> qualified bean's type
 */
public class MatchesTypeQualifier<T> implements Qualifier<T> {
    private static final Logger LOG = LoggerFactory.getLogger(MatchesTypeQualifier.class);

    private static final int UNANNOTATED_ORDER = 10000;

    /**
     * Hierarchy cache. For each class, the complete class hierarchy is cached to avoid costly reflective operations.
     */
    private static final Map<Class<?>, List<Class<?>>> HIERARCHIES = new ConcurrentHashMap<>();

    private final Class<? extends Annotation> annotation;
    private final Class<?> type;
    private final String attributeName;
    private final List<Class<?>> hierarchy;
    private final boolean acceptMissing;

    private MatchesTypeQualifier(Class<? extends Annotation> annotation, String attributeName, Class<?> type, boolean acceptMissing) {
        this.annotation = annotation;
        this.type = type;
        this.attributeName = attributeName;
        this.acceptMissing = acceptMissing;
        hierarchy = HIERARCHIES.computeIfAbsent(type, k -> ClassUtils.resolveHierarchy(type));
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
        return new MatchesTypeQualifier<>(annotation, attributeName, type, false);
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
        return new MatchesTypeQualifier.Closest<>(annotation, attributeName, type, false);
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
        return new MatchesTypeQualifier<>(annotation, attributeName, type, true);
    }

    @Override
    public <BT extends BeanType<T>> Stream<BT> reduce(Class<T> beanType, Stream<BT> candidates) {
        return processStream(candidates
                .filter(candidate -> beanType.isAssignableFrom(candidate.getBeanType()))
                .map(candidate -> {
                    Class<?> marker = getTypeMarker(beanType, candidate);

                    int result = compare(marker);
                    if (LOG.isTraceEnabled() && result < 0) {
                        LOG.trace("Bean type {} is not compatible marker type [{}] of candidate {}", beanType, marker, candidate);
                    }
                    return new AbstractMap.SimpleEntry<>(candidate, result);
                })
                .filter(entry -> entry.getValue() != -1)
        ).map(Map.Entry::getKey);
    }

    protected <BT extends BeanType<T>> Stream<AbstractMap.SimpleEntry<BT, Integer>> processStream(Stream<AbstractMap.SimpleEntry<BT, Integer>> candidates) {
        return candidates.sorted(Comparator.comparingInt(AbstractMap.SimpleEntry::getValue));
    }

    private <BT extends BeanType<T>> Class<?> getTypeMarker(Class<T> beanType, BT candidate) {
        AnnotationValue<?> val = candidate.getAnnotation(annotation);
        return val == null ? null : val.getRequiredValue(attributeName, Class.class);
    }

    private int compare(Class<?> marker) {
        if (marker == null) {
            return acceptMissing ? UNANNOTATED_ORDER : -1;
        }
        return hierarchy.indexOf(marker);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MatchesTypeQualifier<?> that = (MatchesTypeQualifier<?>) o;
        return
                Objects.equals(annotation, that.annotation) &&
                        Objects.equals(attributeName, that.attributeName) &&
                        Objects.equals(acceptMissing, that.acceptMissing) &&
                        Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotation, type, acceptMissing, type);
    }

    @Override
    public String toString() {
        return "MatchesTypeQualifier{" +
                "annotation=" + annotation +
                ", attributeName='" + attributeName + '\'' +
                ", type=" + type +
                ", acceptMissing=" + acceptMissing +
                '}';
    }

    protected static class Closest<T> extends MatchesTypeQualifier<T> {
        public Closest(Class<? extends Annotation> annotation, String attributeName, Class<?> type, boolean acceptMissing) {
            super(annotation, attributeName, type, acceptMissing);
        }

        @Override
        protected <BT extends BeanType<T>> Stream<AbstractMap.SimpleEntry<BT, Integer>> processStream(Stream<AbstractMap.SimpleEntry<BT, Integer>> candidates) {
            return candidates.collect(StreamUtils.minAll(
                            Comparator.comparingInt(Map.Entry::getValue),
                            Collectors.toList()))
                    .stream();
        }

        @Override
        public int hashCode() {
            return super.hashCode() ^ 0x999;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            return super.equals(o);
        }
    }
}
