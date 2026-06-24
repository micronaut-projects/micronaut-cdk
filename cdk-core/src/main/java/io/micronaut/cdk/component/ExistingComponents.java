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
package io.micronaut.cdk.component;

import io.micronaut.cdk.util.Assert;
import io.micronaut.core.annotation.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Container for existing {@link Component}s.
 */
public class ExistingComponents implements Iterable<Component<?>> {

    private final Collection<Component<?>> existing;

    /**
     * Constructor.
     *
     * @param existing existing components
     */
    public ExistingComponents(@NonNull Collection<Component<?>> existing) {
        this.existing = Assert.notNull(existing, "existing cannot be null");
    }

    /**
     * Components.
     *
     * @return the components
     */
    @NonNull
    public Collection<Component<?>> getExisting() {
        return Collections.unmodifiableCollection(existing);
    }

    /**
     * The components as a stream.
     *
     * @return the components as a stream
     */
    @NonNull
    public Stream<Component<?>> stream() {
        return existing.stream();
    }

    @NonNull
    @Override
    public Iterator<Component<?>> iterator() {
        return existing.iterator();
    }

    /**
     * Whether there are elements.
     *
     * @return true if no elements
     */
    public boolean isEmpty() {
        return existing.isEmpty();
    }

    @Override
    public String toString() {
        return "ExistingComponents{existing=" + existing + '}';
    }
}
