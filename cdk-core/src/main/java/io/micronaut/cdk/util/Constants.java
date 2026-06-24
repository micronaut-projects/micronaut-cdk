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
 * Cloud-agnostic constants.
 */
public final class Constants {

    /**
     * Tag key used to store the user-specified unique ID to later determine
     * if an Action's resource exists.
     */
    public static final String CDK_ID = "cdk__id__";

    /**
     * If true, check limits before deploying to ensure capacity.
     */
    public static final String CHECK_LIMITS_KEY = "cdk.check-limits";

    /**
     * ApplicationContext config key for the name of the current cloud.
     */
    public static final String CLOUD_KEY = "cdk.cloud";

    /**
     * If true, do all the work except for making changes in the cloud.
     */
    public static final String DRY_RUN_KEY = "cdk.dry-run";

    private Constants() {
        // static only
    }
}
