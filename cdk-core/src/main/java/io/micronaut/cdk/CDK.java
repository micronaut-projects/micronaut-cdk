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
package io.micronaut.cdk;

import io.micronaut.cdk.action.ActionProcessor;
import io.micronaut.cdk.action.Actions;
import io.micronaut.cdk.action.Stack;
import io.micronaut.cdk.command.RunReport;
import io.micronaut.cdk.security.UserInfo;
import io.micronaut.cdk.security.UserInfoHolder;
import io.micronaut.cdk.util.Assert;
import io.micronaut.cdk.util.CDKUtils;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.NonNull;

import java.util.Map;

import static io.micronaut.cdk.action.Stack.DEFAULT_STACK;
import static io.micronaut.cdk.util.Constants.CHECK_LIMITS_KEY;
import static io.micronaut.cdk.util.Constants.CLOUD_KEY;
import static io.micronaut.cdk.util.Constants.DRY_RUN_KEY;

/**
 * Main entry point.
 */
public final class CDK {

    static {
        CDKUtils.configureLogging();
    }

    private CDK() {
        // static only
    }

    /**
     * Resolves the work to do based on provided actions, does the work, and generates a report.
     *
     * @param actions  the actions
     * @param userInfo user auth info
     * @param options  user provided options
     * @return the report
     */
    @NonNull
    public static RunReport process(@NonNull Actions actions,
                                    @NonNull UserInfo userInfo,
                                    @NonNull CdkProcessOptions options) {
        Assert.notNull(actions, "Actions cannot be null");
        Assert.notNull(options, "CdkProcessOptions cannot be null");

        UserInfoHolder.set(Assert.notNull(userInfo, "UserInfo cannot be null"));

        Map<String, Object> config = Map.of(
                CHECK_LIMITS_KEY, options.getCheckLimits(),
                CLOUD_KEY, userInfo.getCloud().name(),
                DRY_RUN_KEY, options.isDryRun()
        );

        try (var ctx = ApplicationContext.run(config)) {
            return ctx.getBean(ActionProcessor.class).process(actions, options);
        }
    }

    /**
     * Resolves the work to do based on provided stack, does the work, and generates a report.
     *
     * @param stackName the stack to deploy
     * @param userInfo  user auth info
     * @param options   user provided options
     * @return the report
     */
    @NonNull
    public static RunReport process(@NonNull String stackName,
                                    @NonNull UserInfo userInfo,
                                    @NonNull CdkProcessOptions options) {
        return process(new Actions(Stack.get(stackName).getActions()), userInfo, options);
    }

    /**
     * Resolves the work to do based on the default stack, does the work,
     * and generates a report.
     * <p>
     * This method uses the provided {@code dryRun} value, while all other options
     * use their default values from {@link CdkProcessOptions}.
     *
     * @param userInfo user auth info
     * @param options  user provided options
     * @return the report
     */
    @NonNull
    public static RunReport process(@NonNull UserInfo userInfo,
                                    @NonNull CdkProcessOptions options) {
        return process(DEFAULT_STACK, userInfo, options);
    }
}
