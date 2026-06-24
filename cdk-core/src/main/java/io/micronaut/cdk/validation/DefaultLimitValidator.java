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
package io.micronaut.cdk.validation;

import io.micronaut.cdk.ExecutionContext;
import io.micronaut.cdk.util.Assert;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

import java.util.Collection;

import static io.micronaut.cdk.util.Constants.CHECK_LIMITS_KEY;
import static io.micronaut.core.util.StringUtils.FALSE;

/**
 * Delegates to injected {@link LimitChecker}s to check limits.
 */
@Singleton
@Requires(property = CHECK_LIMITS_KEY, notEquals = FALSE)
public class DefaultLimitValidator implements LimitValidator {

    private final Collection<LimitChecker> limitCheckers;

    private final ExecutionContext ec;

    DefaultLimitValidator(Collection<LimitChecker> limitCheckers,
                          ExecutionContext ec) {
        this.limitCheckers = limitCheckers;
        this.ec = ec;
    }

    @Override
    public void verify() throws LimitValidationException {
        Assert.notNull(ec, "ExecutionContext cannot be null");

        for (var limitChecker : limitCheckers) {
            limitChecker.check(ec.getDelta());
        }
    }
}
