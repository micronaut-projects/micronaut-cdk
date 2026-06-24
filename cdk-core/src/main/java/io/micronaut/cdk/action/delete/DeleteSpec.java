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
package io.micronaut.cdk.action.delete;

import io.micronaut.cdk.action.Action;
import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.component.Component;
import io.micronaut.cdk.component.ComponentReference;
import io.micronaut.cdk.util.Assert;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

import java.util.Collection;

/**
 * Base class for delete specs.
 *
 * @param <D> the spec type
 */
public abstract class DeleteSpec<D extends DeleteSpec<D>> extends Action<D> {

    private final boolean byCdkId;
    private final boolean errorIfNotFound;
    private final String id;
    @Nullable
    private final String newName;
    @Nullable
    private final ComponentReference parentReference;
    @Nullable
    private final Class<? extends Component> componentClass;

    /**
     * Constructor.
     *
     * @param id              id
     * @param stacks          stacks
     * @param byCdkId         {@code true} if id is a CDK ID, false if a cloud ID
     * @param errorIfNotFound if {@code true} throw an exception if the specified
     *                        instance doesn't exist
     * @param newName         optional new name
     */
    protected DeleteSpec(@NonNull String id,
                         @NonNull Collection<String> stacks,
                         boolean byCdkId,
                         boolean errorIfNotFound,
                         @Nullable String newName) {
        this(id, null, stacks, byCdkId, errorIfNotFound, newName, null);
    }

    /**
     * Constructor.
     *
     * @param id              id
     * @param stacks          stacks
     * @param byCdkId         {@code true} if id is a CDK ID, false if a cloud ID
     * @param errorIfNotFound if {@code true} throw an exception if the specified
     *                        instance doesn't exist
     * @param newName         optional new name
     * @param componentClass  class of the cloud Component associated with the spec.
     */
    protected DeleteSpec(@NonNull String id,
                         @Nullable Class<? extends Component> componentClass,
                         @NonNull Collection<String> stacks,
                         boolean byCdkId,
                         boolean errorIfNotFound,
                         @Nullable String newName) {
        this(id, componentClass, stacks, byCdkId, errorIfNotFound, newName, null);
    }

    /**
     * Constructor.
     *
     * @param id              id
     * @param stacks          stacks
     * @param byCdkId         {@code true} if id is a CDK ID, false if a cloud ID
     * @param errorIfNotFound if {@code true} throw an exception if the specified
     *                        instance doesn't exist
     * @param newName         optional new name
     * @param parentReference optional parent reference (e.g. a reference for a Log's LogGroup)
     */
    protected DeleteSpec(@NonNull String id,
                         @NonNull Collection<String> stacks,
                         boolean byCdkId,
                         boolean errorIfNotFound,
                         @Nullable String newName,
                         @Nullable ComponentReference parentReference) {
        this(id, null, stacks, byCdkId, errorIfNotFound, newName, parentReference);
    }

    /**
     * Constructor.
     *
     * @param id              id
     * @param stacks          stacks
     * @param byCdkId         {@code true} if id is a CDK ID, false if a cloud ID
     * @param errorIfNotFound if {@code true} throw an exception if the specified
     *                        instance doesn't exist
     * @param newName         optional new name
     * @param parentReference optional parent reference (e.g. a reference for a Log's LogGroup)
     * @param componentClass  class of the cloud Component associated with the spec.
     */
    protected DeleteSpec(@NonNull String id,
                         @Nullable Class<? extends Component> componentClass,
                         @NonNull Collection<String> stacks,
                         boolean byCdkId,
                         boolean errorIfNotFound,
                         @Nullable String newName,
                         @Nullable ComponentReference parentReference) {
        super("Delete(" + (byCdkId ? "CDK" : "Cloud") + " ID: " + id + ')', stacks);
        this.id = Assert.hasText(id, "id is required");
        this.byCdkId = byCdkId;
        this.errorIfNotFound = errorIfNotFound;
        this.newName = newName;
        this.parentReference = parentReference;
        this.componentClass = componentClass;
    }

    /**
     * ID.
     *
     * @return the id
     */
    @NonNull
    public String getId() {
        return id;
    }

    /**
     * The new name (optional).
     *
     * @return the name
     */
    @Nullable
    public String getNewName() {
        return newName;
    }

    /**
     * Whether the id is a CDK ID or cloud ID.
     *
     * @return true if the id is a CDK ID
     */
    public boolean isByCdkId() {
        return byCdkId;
    }

    /**
     * Throw an exception if the specified instance doesn't exist.
     *
     * @return true to throw an exception
     */
    public boolean isErrorIfNotFound() {
        return errorIfNotFound;
    }

    /**
     * Optional parent/owner reference.
     *
     * @return the reference
     */
    @Nullable
    protected ComponentReference getParentReference() {
        return parentReference;
    }

    /**
     * Returns the Component type of the deleted resource. A DeleteSpec may serve multiple resources,
     * this method may return an override type whose Component is being deleted. The method may
     * return {@code null}, the DeleteSpec instance type should be used to determine the Component
     * instead in some way.
     *
     * @return optional deleted resource Component type.
     */
    @Nullable
    public Class<? extends Component> getComponentClass() {
        return componentClass;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " +
                "id='" + getId() + '\'' +
                ", componentClass=" + componentClass +
                ", errorIfNotFound=" + errorIfNotFound +
                ", byCdkId=" + byCdkId +
                ", newName=" + quote(newName) +
                ", parentReference=" + parentReference;

    }

    /**
     * Creates a Builder for DeleteSpec of a resource identified by 'id'. This
     * Builder will create a generic DeleteSpec that works with just the properties tracked by the base
     * DeleteSpec and no other information. The caller <b>must</b> call {@link Builder#withComponentClass(Class)}
     * to set the type of the component.
     *
     * @param id      resource id.
     * @param byCdkId true, the ID is the CDK ID, false for cloud native ID
     * @param <D>     the delete spec type
     * @param <B>     the builder type
     * @return configured Builder
     */
    @SuppressWarnings({"unchecked"})
    public static <D extends DeleteSpec<D>, B extends DeleteSpec.Builder<D, B>> DeleteSpec.Builder<D, B> defaultBuilder(
            @NonNull String id,
            boolean byCdkId) {
        return (B) new SimpleDeleteSpecBuilder<D>(id, byCdkId);
    }

    /**
     * Creates a Builder for DeleteSpec fpr an existing cloud resource identified by the Component. This
     * Builder will create a generic DeleteSpec that works with just the properties tracked by the base
     * DeleteSpec and no other information.
     *
     * @param component the existing resource.
     * @param <D>       the delete spec type
     * @param <B>       the builder type
     * @param <N>       cloud data type
     * @param <C>       component type
     * @return configured Builder
     */
    @SuppressWarnings({"unchecked"})
    public static <D extends DeleteSpec<D>, B extends DeleteSpec.Builder<D, B>, N, C extends Component<N>> B defaultBuilder(
            @NonNull C component) {
        if (component.getCloudId() == null) {
            throw new IllegalArgumentException("Cloud ID is required");
        }
        return (B) new SimpleDeleteSpecBuilder<D>(component.getCloudId(), false)
                .withComponentClass(component.getClass());
    }

    /**
     * Builder.
     *
     * @param <D> the spec type
     * @param <B> the builder type
     */
    public abstract static class Builder<D extends DeleteSpec<D>, B extends Builder<D, B>> extends Action.Builder<D, B> {

        private final boolean byCdkId;
        private final String id;

        @Nullable
        private String newName;
        private boolean errorIfNotFound = true;
        @Nullable
        private ComponentReference parentReference;
        @Nullable
        private Class<? extends Component> componentClass;

        /**
         * Constructor.
         *
         * @param id      id
         * @param byCdkId {@code true} if id is a CDK ID, false if a cloud ID
         */
        protected Builder(@NonNull String id, boolean byCdkId) {
            super("Delete(" + (byCdkId ? "CDK" : "Cloud") + " ID: " + id + ')');
            this.id = Assert.hasText(id, "id is required");
            this.byCdkId = byCdkId;
        }

        /**
         * Constructor.
         *
         * @param id             id
         * @param byCdkId        {@code true} if id is a CDK ID, false if a cloud ID
         * @param componentClass class of the component.
         */
        protected Builder(@NonNull String id, boolean byCdkId, Class<? extends Component> componentClass) {
            super("Delete(" + (byCdkId ? "CDK" : "Cloud") + " ID: " + id + ')');
            this.id = Assert.hasText(id, "id is required");
            this.byCdkId = byCdkId;
            this.componentClass = Assert.notNull(componentClass, "componentClass is required");
        }

        /**
         * Associates the DeleteSpec with a specific Component Class. This should be done only for generic
         * DeleteSpecs that serve as a description for multiple Components. The class can be set only once:
         * if a specific Builder subclass fixes the component Class, an attempt to change will thrown an
         * exception.
         *
         * @param componentClass the component Class
         * @return this instance.
         */
        public B withComponentClass(@NonNull Class<? extends Component> componentClass) {
            if (this.componentClass != null && this.componentClass != componentClass) {
                throw new IllegalArgumentException("Component class mismatch");
            }
            this.componentClass = componentClass;
            return self();
        }

        /**
         * ID.
         *
         * @return the id
         */
        @NonNull
        public String getId() {
            return id;
        }

        /**
         * Whether the id is a CDK ID or cloud ID.
         *
         * @return true if the id is a CDK ID
         */
        public boolean isByCdkId() {
            return byCdkId;
        }

        /**
         * The new name (optional).
         *
         * @param newName the name
         * @return this
         */
        @NonNull
        public B newName(@NonNull String newName) {
            this.newName = newName;
            return self();
        }

        /**
         * The new name (optional).
         *
         * @return the name
         */
        @Nullable
        public String getNewName() {
            return newName;
        }

        /**
         * Throw an exception if the specified instance doesn't exist.
         *
         * @param errorIfNotFound true to throw an exception
         * @return this
         */
        @NonNull
        public B errorIfNotFound(boolean errorIfNotFound) {
            this.errorIfNotFound = errorIfNotFound;
            return self();
        }

        /**
         * Throw an exception if the specified instance doesn't exist.
         *
         * @return true to throw an exception
         */
        public boolean isErrorIfNotFound() {
            return errorIfNotFound;
        }

        /**
         * Optional parent/owner reference.
         *
         * @param parentReference the reference
         * @return this
         */
        @NonNull
        public B parentReference(@Nullable ComponentReference parentReference) {
            this.parentReference = parentReference;
            return self();
        }

        /**
         * Optional parent/owner reference.
         *
         * @return the reference
         */
        @Nullable
        public ComponentReference getParentReference() {
            return parentReference;
        }

        /**
         * Returns the Component type of the to-be-deleted resource.
         *
         * @return the componen type
         */
        @Nullable
        public Class<? extends Component> getComponentClass() {
            return componentClass;
        }
    }

    /**
     * Simple concrete class of the DeleteSpec.
     */
    private static class SimpleDeleteSpec extends DeleteSpec<SimpleDeleteSpec> {
        protected SimpleDeleteSpec(String id, Collection<String> stacks, boolean byCdkId, boolean errorIfNotFound, @Nullable String newName, @Nullable ComponentReference parentReference, @Nullable Class<? extends Component> componentClass) {
            super(id, componentClass, stacks, byCdkId, errorIfNotFound, newName, parentReference);
        }
    }

    /**
     * Simple implementation of the Builder that produces a DeleteSpec.
     *
     * @param <T> the spec type
     */
    private static class SimpleDeleteSpecBuilder<T extends DeleteSpec<T>> extends Builder<T, SimpleDeleteSpecBuilder<T>> {
        public SimpleDeleteSpecBuilder(String id, boolean byCdkId) {
            super(id, byCdkId);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected T doBuild() throws InvalidActionException {
            return (T) new SimpleDeleteSpec(
                    getId(),
                    getStacks(),
                    isByCdkId(),
                    isErrorIfNotFound(),
                    getNewName(),
                    getParentReference(),
                    getComponentClass()
            );
        }
    }

}
