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
package io.micronaut.cdk.action;

import io.micronaut.cdk.CdkException;

import java.io.Serial;

/**
 * Indicates a validation problem with an {@link Action}.
 */
public class InvalidActionException extends CdkException {
    @Serial
    private static final long serialVersionUID = 1;

    /**
     * Constructor.
     *
     * @param cdkId   CDK ID
     * @param message message
     */
    public InvalidActionException(String cdkId, String message) {
        this(cdkId, message, null);
    }

    /**
     * Constructor.
     *
     * @param cdkId CDK ID
     * @param cause cause
     */
    public InvalidActionException(String cdkId, Throwable cause) {
        this(cdkId, cause.getMessage(), cause);
    }

    /**
     * Constructor.
     *
     * @param cdkId   CDK ID
     * @param message message
     * @param cause   cause
     */
    public InvalidActionException(String cdkId, String message, Throwable cause) {
        super("Invalid action '" + cdkId + "': " + message, cause);
    }
}
