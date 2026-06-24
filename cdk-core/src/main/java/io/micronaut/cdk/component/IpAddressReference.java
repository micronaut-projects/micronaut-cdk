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

/**
 * For retrieving a public or private IP address from an existing component.
 */
public class IpAddressReference {

    private final String cdkId;
    private final boolean privateAddress;
    private final String variable;

    /**
     * Constructor.
     *
     * @param cdkId          the CDK ID
     * @param privateAddress true for the private IP address, false for public
     * @param variable       the variable name that will be dereferenced
     */
    public IpAddressReference(@NonNull String cdkId,
                              boolean privateAddress,
                              @NonNull String variable) {
        this.cdkId = Assert.notNull(cdkId, "cdkId cannot be null");
        this.privateAddress = privateAddress;
        this.variable = Assert.notNull(variable, "variable cannot be null");
    }

    /**
     * CDK ID.
     *
     * @return the CDK ID
     */
    @NonNull
    public String getCdkId() {
        return cdkId;
    }

    /**
     * Whether it's a private or public address.
     *
     * @return true if private
     */
    public boolean isPrivateAddress() {
        return privateAddress;
    }

    /**
     * The variable.
     *
     * @return the variable
     */
    @NonNull
    public String getVariable() {
        return variable;
    }

    @Override
    public String toString() {
        return "IpAddressReference{" +
                "cdkId='" + cdkId + '\'' +
                ", privateAddress=" + privateAddress +
                ", variable='" + variable + '\'' +
                '}';
    }
}
