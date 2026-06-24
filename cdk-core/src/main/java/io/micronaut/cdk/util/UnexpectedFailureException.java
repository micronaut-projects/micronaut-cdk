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

/**
 * Unspecified exception, not necessarily cloud-related. Use this Exception to wrap failures
 * that should propagate upwards the stack and should be handled centrally at resource or script
 * level.
 */
public class UnexpectedFailureException extends RuntimeException {
    /**
     * Initializes the exception.
     *
     * @param message the exception message.
     */
    public UnexpectedFailureException(String message) {
        super(message);
    }

    /**
     * Initializes the exception.
     *
     * @param message The exception's message
     * @param cause   the original throwable.
     */
    public UnexpectedFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
