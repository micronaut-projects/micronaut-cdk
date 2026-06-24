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
package io.micronaut.cdk.test

import spock.lang.Specification

/**
 * Compatibility base for Spock specs compiled with Groovy 5 against the Groovy 4 Spock artifact.
 */
abstract class CdkSpecification extends Specification {

    protected <T extends Throwable> T thrownImpl(Object inferredName, Class<T> type) {
        thrownFromContext(type)
    }

    protected <T extends Throwable> T thrownImpl(Object name, Object inferredName, Class<T> type) {
        thrownFromContext(type)
    }

    private <T extends Throwable> T thrownFromContext(Class<T> type) {
        Throwable thrownException = specificationContext.thrownException
        if (thrownException == null) {
            throw new AssertionError("Expected exception of type '${type.name}', but no exception was thrown")
        }
        if (!type.isInstance(thrownException)) {
            throw thrownException
        }
        type.cast(thrownException)
    }
}
