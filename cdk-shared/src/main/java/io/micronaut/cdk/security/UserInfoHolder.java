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
package io.micronaut.cdk.security;

/**
 * Thread-local holder for the current {@link UserInfo}.
 */
public final class UserInfoHolder {

    private static final ThreadLocal<UserInfo> HOLDER = new ThreadLocal<>();

    private UserInfoHolder() {
        // static only
    }

    /**
     * Set the current info for the duration of a scoped operation and clear it afterwards.
     *
     * @param userInfo info to set for the scope
     * @return an {@link AutoCloseable} that removes the info when closed
     */
    public static AutoCloseable withUserInfo(UserInfo userInfo) {
        HOLDER.set(userInfo);
        return HOLDER::remove;
    }

    /**
     * Set the current info.
     *
     * @param userInfo info
     */
    public static void set(UserInfo userInfo) {
        HOLDER.set(userInfo);
    }

    /**
     * Get the current info.
     *
     * @return info
     */
    public static UserInfo get() {
        return HOLDER.get();
    }

    /**
     * Remove the current info.
     */
    public static void clear() {
        HOLDER.remove();
    }
}
