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
package io.micronaut.cdk.action.ssh;

import io.micronaut.cdk.CdkException;

import java.io.Serial;

/**
 * Represents a problem when running SSH exec or SCP.
 */
public class SSHException extends CdkException {
    @Serial
    private static final long serialVersionUID = 1;

    /**
     * Constructor.
     *
     * @param message message
     */
    public SSHException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message message
     * @param cause   cause
     */
    public SSHException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     *
     * @param cause cause
     */
    public SSHException(Throwable cause) {
        super(cause);
    }
}
