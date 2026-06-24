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
package io.micronaut.cdk.command;

import io.micronaut.cdk.ExecutionContext;
import io.micronaut.cdk.action.Executable;
import io.micronaut.cdk.action.Executable.Result;
import io.micronaut.cdk.util.Assert;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;

/**
 * Default implementation of {@link ExecutableRunner}.
 */
@Singleton
public class DefaultExecutableRunner implements ExecutableRunner {

    private final ExecutionContext ec;

    DefaultExecutableRunner(@NonNull ExecutionContext ec) {
        this.ec = Assert.notNull(ec, "ExecutionContext cannot be null");
    }

    @Override
    @NonNull
    public List<Result> run(@NonNull Executable<?> executable) {
        Assert.notNull(executable, "executable cannot be null");
        Assert.notNull(ec, "ExecutionContext cannot be null");

        List<Result> results = executable.execute(ec);

        Optional<Result> failed = results.stream().filter(it -> it.getException() != null).findAny();
        if (failed.isPresent()) {
            throw new CommandRunException(failed.get().getException());
        }

        return results;
    }
}
