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

import io.micronaut.cdk.component.ComponentReference;
import io.micronaut.core.annotation.UsedByGeneratedCode;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Various implementation utilities.
 */
@UsedByGeneratedCode
public final class CdkImplUtils {
    private CdkImplUtils() {
    }

    /**
     * Adds a reference to a collection, if not null. This is a helper to filter out null references, used by
     * generated code. If the target collection is null, the method will create a List collection. The method
     * will add all non-null references to the collection and return the modified collection.
     *
     * @param col     the target collection, possibly {@code null}
     * @param addRefs references to add.
     * @return modified collection
     */

    public static Collection<ComponentReference> addReferences(Collection<ComponentReference> col, ComponentReference... addRefs) {
        if (col == null) {
            col = new ArrayList<>();
        }
        if (addRefs == null) {
            return col;
        }
        for (ComponentReference ref : addRefs) {
            if (ref != null) {
                if (!col.contains(ref)) {
                    col.add(ref);
                }
            }
        }
        return col;
    }

    /**
     * Adds a reference to a collection, if not null. This is a helper to filter out null references, used by
     * generated code. If the target collection is null, the method will create a List collection. The method
     * will add all non-null ComponentReferences from passed collections to the target collection
     * and return the modified target collection.
     *
     * @param col            the target collection, possibly {@code null}
     * @param refCollections collections to add from
     * @return modified collection
     */
    public static Collection<ComponentReference> addReferences(Collection<ComponentReference> col, Iterable<ComponentReference>... refCollections) {
        if (col == null) {
            col = new ArrayList<>();
        }
        if (refCollections == null) {
            return col;
        }
        for (Iterable<ComponentReference> chunk : refCollections) {
            if (chunk == null) {
                continue;
            }
            for (ComponentReference ref : chunk) {
                if (ref != null) {
                    if (!col.contains(ref)) {
                        col.add(ref);
                    }
                }
            }
        }
        return col;
    }
}
