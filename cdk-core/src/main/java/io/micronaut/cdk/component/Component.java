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

import io.micronaut.cdk.Cloud;
import io.micronaut.cdk.Identified;
import io.micronaut.cdk.impl.ComponentAccessor;
import io.micronaut.cdk.util.Assert;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

/**
 * Abstract base class for existing components.
 *
 * @param <N> the cloud component type
 */
public abstract class Component<N> implements Identified {

    private final String cdkId;
    private final N cloudComponent;
    private final String cloudId;

    /**
     * The cloud for the component. This field is intentionally not final.
     * The infrastructure will set it after the Deployer completes.
     */
    private Cloud cloud;

    /**
     * Constructor.
     *
     * @param cloudComponent the cloud component
     * @param cloudId        the cloud ID
     * @param cdkId          CDK ID
     */
    protected Component(@NonNull N cloudComponent,
                        @NonNull String cloudId,
                        @NonNull String cdkId) {
        this.cloudComponent = Assert.notNull(cloudComponent, "cloudComponent cannot be null");
        this.cloudId = Assert.notNull(cloudId, "cloudId cannot be null");
        this.cdkId = Assert.notNull(cdkId, "cdkId cannot be null");
    }

    /**
     * Constructor.
     *
     * @param cloudComponent the cloud component
     * @param cloudId        the cloud ID
     * @param cdkId          CDK ID
     * @param cloud          the cloud type
     */
    protected Component(@NonNull N cloudComponent,
                        @NonNull String cloudId,
                        @NonNull String cdkId,
                        @NonNull Cloud cloud) {
        this.cloudComponent = Assert.notNull(cloudComponent, "cloudComponent cannot be null");
        this.cloudId = Assert.notNull(cloudId, "cloudId cannot be null");
        this.cdkId = Assert.notNull(cdkId, "cdkId cannot be null");
        this.cloud = Assert.notNull(cloud, "cloud cannot be null");
    }

    /**
     * Returns cloud of the Component instance. May return {@code null} to indicate the default target
     * Cloud is used.
     *
     * @return cloud that holds the Component.
     */
    public @Nullable Cloud getCloud() {
        return cloud;
    }

    /**
     * Cloud component.
     *
     * @return the cloud component
     */
    @NonNull
    public N getCloudComponent() {
        return cloudComponent;
    }

    /**
     * Cloud ID.
     *
     * @return the cloud component ID
     */
    @NonNull
    public String getCloudId() {
        return cloudId;
    }

    /**
     * CDK ID.
     *
     * @return the user-specified unique id
     */
    @NonNull
    @Override
    public String getCdkId() {
        return cdkId;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    /**
     * Same as toString except replace full toString of component / executable / spec
     * with class name and CDK ID.
     *
     * @return compact toString
     */
    public String toCompactString() {
        return toString(true);
    }

    private String toString(boolean compact) {

        String cloudComponentString = compact
                ? cloudComponent.getClass().getName() + '(' + cloudId + ')'
                : cloudComponent.toString();

        return getClass().getSimpleName() + '{' +
                "cdkId='" + cdkId + '\'' +
                ", cloudComponent=" + cloudComponentString +
                '}';
    }

    static {
        // initializes the accessor.
        new AccessorImpl();
    }

    /**
     * Implementation of accessor interface to set Component internals.
     */
    private static final class AccessorImpl extends ComponentAccessor {
        @Override
        public void setCloud(Component<?> c, Cloud cloud) {
            if (cloud == null) {
                throw new IllegalArgumentException("cloud cannot be null");
            }
            c.cloud = cloud;
        }
    }
}
