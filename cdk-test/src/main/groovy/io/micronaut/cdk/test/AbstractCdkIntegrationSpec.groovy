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

import io.micronaut.context.ApplicationContext
import spock.lang.Shared

import static io.micronaut.cdk.test.TestConstants.TEST_NAME_KEY
import static io.micronaut.context.env.Environment.TEST

/**
 * Base class for integration tests that start an {@link ApplicationContext}.
 */
abstract class AbstractCdkIntegrationSpec extends AbstractCdkSpec {

    @Shared
    protected ApplicationContext applicationContext

    /**
     * Build an application context for the test env.
     */
    void setupSpec() {
        applicationContext = ApplicationContext
                .builder(configuration, TEST)
                .packages('io.micronaut.cdk')
                .start()
    }

    /**
     * Close the ApplicationContext.
     */
    void cleanupSpec() {
        applicationContext?.close()
    }

    /**
     * Configuration properties used to build the application context.
     *
     * @return the properties
     */
    protected Map<String, Object> getConfiguration() {
        [(TEST_NAME_KEY): getClass().simpleName]
    }
}
