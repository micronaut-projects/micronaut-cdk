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
package io.micronaut.cdk.util;

import io.micronaut.core.annotation.Nullable;

/**
 * Removes or obscures parts of strings (e.g. passwords) that shouldn't appear in output.
 */
@FunctionalInterface
public interface StringCleaner {

    /**
     * Clean a string, e.g. to mask passwords.
     *
     * @param s the string
     * @return the cleaned string
     */
    @Nullable
    String clean(@Nullable String s);
}
