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

import io.micronaut.cdk.Cloud;
import io.micronaut.cdk.security.UserInfo;
import io.micronaut.cdk.security.UserInfoHolder;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.StringUtils;

import java.io.File;

/**
 * Assertion methods. Based on org.springframework.util.Assert.
 */
public abstract class Assert {

    private Assert() {
        // static only
    }

    /**
     * Throw an IllegalStateException if not authenticated.
     *
     * @return the user info
     */
    public static UserInfo authenticated() {
        UserInfo userInfo = UserInfoHolder.get();
        state(userInfo != null, "Must be authenticated");
        return userInfo;
    }

    /**
     * Throw an IllegalStateException if not authenticated for the specified cloud.
     *
     * @param cloud the cloud
     * @return the user info
     */
    public static UserInfo authenticated(@NonNull Cloud cloud) {
        notNull(cloud, "Cloud is required");
        UserInfo userInfo = authenticated();
        state(userInfo.getCloud() == cloud,
                "Must be authenticated to " + cloud + ", not " + userInfo.getCloud());
        return userInfo;
    }

    /**
     * Throw an IllegalStateException if the file does not exist.
     *
     * @param file the file
     */
    public static void exists(@NonNull File file) {
        notNull(file, "file is required");
        if (!file.exists()) {
            throw new IllegalStateException("File " + file + " doesn't exist");
        }
    }

    /**
     * Throw an IllegalArgumentException if the object is not an instance of the specified type.
     *
     * @param type   the type
     * @param object the object
     * @param <T>    the object type
     * @return the object
     */
    @SuppressWarnings("unchecked")
    public static <T> T isInstanceOf(@NonNull Class<T> type, @Nullable Object object) {
        notNull(type, "Type is required");
        if (!type.isInstance(object)) {
            String className = object == null ? "null" : object.getClass().getName();
            throw new IllegalArgumentException("Object of class [" + className + "] must be an instance of " + type);
        }
        return (T) object;
    }

    /**
     * Throw an IllegalArgumentException with the specified message if the object is not null.
     *
     * @param object  the object
     * @param message the message
     */
    public static void isNull(@Nullable Object object, String message) {
        if (object != null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Throw an IllegalArgumentException with the specified message if the object is null.
     *
     * @param object  the object
     * @param message the message
     * @param <T>     the object type
     * @return the object
     */
    public static <T> T notNull(@Nullable T object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
        return object;
    }

    /**
     * Throw an IllegalArgumentException with the specified message if the
     * string is null or only has whitespace characters.
     *
     * @param s       the string
     * @param message the message
     * @return the string
     */
    public static String hasText(@Nullable String s, String message) {

        notNull(s, message);

        if (!StringUtils.hasText(s)) {
            throw new IllegalArgumentException(message);
        }

        return s;
    }

    /**
     * Throw an IllegalStateException with the specified message if the expression is {@code false}.
     *
     * @param expression expression to test
     * @param message    the message
     */
    public static void state(boolean expression, @NonNull String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }
}
