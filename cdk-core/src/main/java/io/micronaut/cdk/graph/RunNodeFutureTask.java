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
package io.micronaut.cdk.graph;

import io.micronaut.cdk.command.Command;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A {@link FutureTask} that executes a {@link Command} associated with a {@link Node}
 * and invokes the provided callbacks upon completion.
 * <p>
 * On successful execution, the {@code onSuccessCallback} is called with the {@link Node}.
 * <p>
 * On failure  the {@code onFailureCallback} is called with the {@link Node}
 * and the error {@link Throwable}.
 */
class RunNodeFutureTask extends FutureTask<Command> {

    private final Consumer<Node> onSuccessCallback;
    private final BiConsumer<Node, Throwable> onFailureCallback;
    private final Node node;

    RunNodeFutureTask(Callable<Command> callable,
                      Node node,
                      Consumer<Node> onSuccessCallback,
                      BiConsumer<Node, Throwable> onFailureCallback) {
        super(callable);
        this.node = node;
        this.onSuccessCallback = onSuccessCallback;
        this.onFailureCallback = onFailureCallback;
    }

    @Override
    protected void done() {
        try {
            get();
            onSuccessCallback.accept(node);
        } catch (CancellationException | InterruptedException | ExecutionException e) {
            onFailureCallback.accept(node, e);
        }
    }
}
