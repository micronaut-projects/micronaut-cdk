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
package io.micronaut.cdk.retry;

import io.micronaut.retry.RetryState;
import io.micronaut.retry.event.RetryEvent;
import io.micronaut.retry.event.RetryEventListener;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs retry events.
 */
@Singleton
public class CdkRetryEventLogger implements RetryEventListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    CdkRetryEventLogger() {
    }

    @Override
    public void onApplicationEvent(RetryEvent e) {
        RetryState state = e.getRetryState();
        logger.debug("RetryEvent ({}) exception: {}, maxAttempts: {}, currentAttempt: {}, " +
                        "multiplier: {}, delay: {}, overallDelay: {}, maxDelay: {}",
                e.getSource().getDescription(false),
                e.getThrowable(),
                state.getMaxAttempts(),
                state.currentAttempt(),
                state.getMultiplier(),
                state.getDelay(),
                state.getOverallDelay(),
                state.getMaxDelay());
    }
}
