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

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

import java.util.Collection;

/**
 * Un-deploy by CDK ID or cloud ID.
 *
 * @param <D> the delete spec type
 */
public class ObjectStorageBucketDeleteSpec<D extends ObjectStorageBucketDeleteSpec<D>> extends DeleteSpec<D> {

    private final String bucketName;

    /**
     * @param id              id
     * @param stacks          stacks
     * @param byCdkId         {@code true} if id is a CDK ID, false if a cloud ID
     * @param errorIfNotFound if {@code true} throw an exception if the specified
     *                        instance doesn't exist
     * @param newName         optional new name
     * @param bucketName      bucket name
     */
    public ObjectStorageBucketDeleteSpec(@NonNull String id,
                                         @NonNull Collection<String> stacks,
                                         boolean byCdkId,
                                         boolean errorIfNotFound,
                                         @Nullable String newName,
                                         @NonNull String bucketName) {
        super(id, stacks, byCdkId, errorIfNotFound, newName);
        this.bucketName = bucketName;
    }

    /**
     * Bucket name.
     *
     * @return the name
     */
    @NonNull
    public String getBucketName() {
        return bucketName;
    }

    @Override
    public String toString() {
        return super.toString() + ", bucketName='" + bucketName + '\'';
    }

    /**
     * Builder.
     *
     * @param id         the id
     * @param byCdkId    true if the id is a CDK ID, false if a cloud ID
     * @param bucketName the bucket name
     * @param <D>        the delete spec type
     * @param <B>        the builder type
     * @return a new builder
     */
    @NonNull
    public static <D extends ObjectStorageBucketDeleteSpec<D>, B extends Builder<D, B>> Builder<D, B> builder(
            @NonNull String id,
            boolean byCdkId,
            @NonNull String bucketName) {
        return new Builder<>(id, byCdkId, bucketName);
    }

    /**
     * Builder.
     *
     * @param <D> the spec type
     * @param <B> the builder type
     */
    public static class Builder<D extends ObjectStorageBucketDeleteSpec<D>, B extends Builder<D, B>> extends DeleteSpec.Builder<D, B> {

        private final String bucketName;

        /**
         * Constructor.
         *
         * @param id         ID
         * @param byCdkId    {@code true} if id is a CDK ID, false if a cloud ID
         * @param bucketName bucket name
         */
        protected Builder(@NonNull String id,
                          boolean byCdkId,
                          @NonNull String bucketName) {
            super(id, byCdkId);
            this.bucketName = bucketName;
        }

        /**
         * Bucket name.
         *
         * @return the name
         */
        @NonNull
        public String getBucketName() {
            return bucketName;
        }

        @Override
        @NonNull
        @SuppressWarnings("unchecked")
        protected D doBuild() throws InvalidActionException {
            return (D) new ObjectStorageBucketDeleteSpec<>(
                    getId(),
                    getStacks(),
                    isByCdkId(),
                    isErrorIfNotFound(),
                    getNewName(),
                    getBucketName()
            );
        }

        @Override
        protected void validate() throws InvalidActionException {
            super.validate();

            requireHasText("bucketName", getBucketName());
        }
    }
}
