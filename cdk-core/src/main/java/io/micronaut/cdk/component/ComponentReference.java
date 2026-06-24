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

import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.util.Assert;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

/**
 * For component lookups when deploying.
 */
public class ComponentReference {

    /**
     * Choose any when deploying.
     */
    public static final ComponentReference ANY = new ComponentReference(null, null, true, false);

    /**
     * No instance.
     */
    public static final ComponentReference NONE = new ComponentReference(null, null, false, true);

    private final String cdkId;
    private final String cloudId;
    private final boolean any;
    private final boolean none;

    ComponentReference(@Nullable String cdkId, @Nullable String cloudId, boolean any, boolean none) {
        Assert.state(!any || !none, "any and none cannot both be true");
        Assert.state(cloudId == null || cdkId == null, "Cannot specify both cloud id and CDK ID");
        Assert.state(!((cloudId != null || cdkId != null) && (any || none)), "Cannot specify both none or any and cloud id or CDK ID");
        Assert.state(cloudId != null || cdkId != null || any || none, "Must specify cloud id or CDK ID or any=true or none=true");
        this.cdkId = cdkId;
        this.cloudId = cloudId;
        this.any = any;
        this.none = none;
    }

    /**
     * Find an existing component by its cloud id (e.g. OCID for OCI).
     *
     * @param cloudId the cloud id
     * @return the reference
     */
    @NonNull
    public static ComponentReference forCloudId(@NonNull String cloudId) {
        return new ComponentReference(null, Assert.notNull(cloudId, "cloudId cannot be null"), false, false);
    }

    /**
     * Find an existing component by its CDK ID.
     *
     * @param cdkId the CDK ID
     * @return the reference
     */
    @NonNull
    public static ComponentReference forCdkId(@NonNull String cdkId) {
        return new ComponentReference(Assert.notNull(cdkId, "cdkId cannot be null"), null, false, false);
    }

    /**
     * Parse a reference string into a {@link ComponentReference}.
     *
     * @param value the string value
     * @return the parsed reference
     */
    @NonNull
    public static ComponentReference parse(@NonNull String value) {
        String trimmed = Assert.hasText(value, "Component reference value cannot be blank").trim();

        if ("any".equals(trimmed)) {
            return ANY;
        }

        if ("none".equals(trimmed)) {
            return NONE;
        }

        if (trimmed.startsWith("cdk:")) {
            String cdkId = trimmed.substring("cdk:".length()).trim();
            if (cdkId.isEmpty()) {
                throw new IllegalArgumentException("Component reference value '" + trimmed + "' must include a CDK ID after 'cdk:'");
            }
            return forCdkId(cdkId);
        }

        if (trimmed.startsWith("cloud:")) {
            String cloudId = trimmed.substring("cloud:".length()).trim();
            if (cloudId.isEmpty()) {
                throw new IllegalArgumentException("Component reference value '" + trimmed + "' must include a cloud ID after 'cloud:'");
            }
            return forCloudId(cloudId);
        }

        throw new IllegalArgumentException("Unsupported component reference value '" + trimmed
                + "'. Supported values: cdk:<id>, cloud:<id>, any, none");
    }

    /**
     * Creates a reference out a Spec. The returned Component uses CDK ID for its identification.
     *
     * @param spec the resource specification
     * @param <T>  spec type.
     * @return ComponentReference instance
     */
    public static <T extends Spec<T>> ComponentReference forSpec(@NonNull T spec) {
        return forCdkId(spec.getCdkId());
    }

    /**
     * CDK ID.
     *
     * @return the CDK ID
     */
    @Nullable
    public String getCdkId() {
        return cdkId;
    }

    /**
     * Cloud id.
     *
     * @return the id
     */
    @Nullable
    public String getCloudId() {
        return cloudId;
    }

    /**
     * Whether to choose any.
     *
     * @return true to choose any
     */
    public boolean isAny() {
        return any;
    }

    /**
     * Whether this is a "none" reference.
     *
     * @return true to not choose
     */
    public boolean isNone() {
        return none;
    }

    @Override
    public String toString() {

        if (any) {
            return "ComponentReference(ANY)";
        }

        if (none) {
            return "ComponentReference(NONE)";
        }

        if (cdkId != null) {
            return "ComponentReference(CDK ID: " + cdkId + ')';
        }

        return "ComponentReference(Cloud ID: " + cloudId + ')';
    }
}
