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

import io.micronaut.cdk.ExecutionContext;
import io.micronaut.cdk.command.Command;
import io.micronaut.cdk.command.TaskFactory;
import io.micronaut.core.annotation.NonNull;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static io.micronaut.cdk.command.Command.Status.SCHEDULED;
import static io.micronaut.cdk.command.Command.Status.SKIPPED;
import static io.micronaut.cdk.command.Command.Status.SUCCESSFUL;
import static io.micronaut.cdk.command.Command.Status.WAITING;
import static io.micronaut.cdk.graph.GraphExecutor.State.FINISHED;
import static io.micronaut.cdk.graph.GraphExecutor.State.READY;
import static io.micronaut.cdk.graph.GraphExecutor.State.RUNNING;

/**
 * Manages the execution of the nodes.
 */
public class GraphExecutor {

    private final ExecutorService executor;
    private final ExecutionContext ec;
    private final TaskFactory<Command> factory;

    private final Set<Node> readyNodes = new LinkedHashSet<>();
    private final Set<Node> runningNodes = new LinkedHashSet<>();
    private final Set<Node> finishedNodes = new LinkedHashSet<>();

    private final Map<Node, AtomicInteger> inDegree = new HashMap<>();
    private final ReentrantLock stateLock = new ReentrantLock();

    private final AtomicBoolean errorOccurred = new AtomicBoolean(false);

    /**
     * Constructor.
     *
     * @param graph    the graph
     * @param executor the thread pool
     * @param ec       execution context
     * @param factory  task factory
     */
    public GraphExecutor(@NonNull Graph graph,
                         @NonNull ExecutorService executor,
                         @NonNull ExecutionContext ec,
                         @NonNull TaskFactory<Command> factory) {
        this.executor = executor;
        this.ec = ec;
        this.factory = factory;
        this.readyNodes.addAll(graph.getInitialNodes());
        for (Node node : graph.getNodes()) {
            inDegree.put(node, new AtomicInteger(node.getInDegree()));
        }
    }

    /**
     * Run the commands.
     */
    public void run() {
        stateLock.lock();
        try {
            initializeCommandsToWaiting();
            // if no runnable commands, end immediately.
            runNextFreeNodesOrShutdown(null);
        } finally {
            stateLock.unlock();
        }
    }

    private void initializeCommandsToWaiting() {
        for (Node it : inDegree.keySet()) {
            it.getCommand().setCommandStatus(WAITING);
        }
    }

    /**
     * Process all currently ready nodes by scheduling them for execution.
     */
    private void executeReadyNodes() {
        Set<Node> nodesToExecute = new LinkedHashSet<>(getNodesWithState(READY));
        setRunning(nodesToExecute);

        for (Node node : nodesToExecute) {
            if (shouldProcess(node)) {
                scheduleNode(node);
            } else {
                skipDependentNodes(node);
            }
        }
    }

    private boolean shouldProcess(Node node) {
        return node.getCommand().getCommandStatus() == WAITING && !errorOccurred.get();
    }

    private void scheduleNode(Node node) {
        Command command = node.getCommand();
        command.setCommandStatus(SCHEDULED);
        Callable<Command> callable = factory.createTask(command);
        executor.submit(new RunNodeFutureTask(
                callable,
                node,
                this::onSuccess,
                this::onFailure
        ));
    }

    /**
     * Mark the node as finished and add successor nodes to readyNodes if all their dependencies are met.
     *
     * @param currentNode the completed node.
     */
    private void markProcessingDone(Node currentNode) {
        setState(currentNode, FINISHED);

        for (Node successor : currentNode.getSuccessors()) {
            int remainingDepCount = -1;
            AtomicInteger depInDegree = inDegree.get(successor);
            if (depInDegree.get() > 0) {
                remainingDepCount = depInDegree.decrementAndGet();
            }

            if (remainingDepCount == 0) {
                setState(successor, State.READY);
            }
        }
    }

    private void markDependentNodeAsSkipped(Node currentNode) {
        for (Node dependent : currentNode.getSuccessors()) {
            dependent.getCommand().setCommandStatus(SKIPPED);
        }
    }

    private void onSuccess(Node node) {
        stateLock.lock();
        try {
            markProcessingDone(node);
            node.getCommand().setCommandStatus(SUCCESSFUL);
            runNextFreeNodesOrShutdown(node);
        } finally {
            stateLock.unlock();
        }
    }

    private void onFailure(Node node, Throwable error) {
        stateLock.lock();
        try {
            ec.registerFailedCommand(node.getCommand(), error);
            switch (ec.getFailureMode()) {
                case ROLLBACK:
                case FAIL_FAST:
                    errorOccurred.set(true);
                case LAZY_FAILURE:
                    skipDependentNodes(node);
                    break;
                default:
                    throw new IllegalStateException(String.format("Unsupported failure mode: %s", ec.getFailureMode()));
            }
        } finally {
            stateLock.unlock();
        }
    }

    private void skipDependentNodes(Node currentNode) {
        stateLock.lock();
        try {
            markProcessingDone(currentNode);
            markDependentNodeAsSkipped(currentNode);
            runNextFreeNodesOrShutdown(currentNode);
        } finally {
            stateLock.unlock();
        }
    }

    private boolean isExecutionComplete() {
        return getNodesWithState(READY).isEmpty()
                && getNodesWithState(RUNNING).isEmpty();
    }

    private void runNextFreeNodesOrShutdown(Node node) {
        if (isExecutionComplete()) {
            executor.shutdown();
        } else {
            executeReadyNodes();
        }
    }

    private Set<Node> getNodesWithState(State state) {
        return switch (state) {
            case READY -> readyNodes;
            case RUNNING -> runningNodes;
            case FINISHED -> finishedNodes;
        };
    }

    /**
     * Get the node count with the specified state.
     *
     * @param state state
     * @return the node count
     */
    int getNodeCountWithState(State state) {
        return getNodesWithState(state).size();
    }

    private void setState(Node node, State state) {
        switch (state) {
            case READY:
                runningNodes.remove(node);
                finishedNodes.remove(node);
                readyNodes.add(node);
                break;
            case RUNNING:
                readyNodes.remove(node);
                finishedNodes.remove(node);
                runningNodes.add(node);
                break;
            case FINISHED:
                readyNodes.remove(node);
                runningNodes.remove(node);
                finishedNodes.add(node);
                break;
            default:
                throw new IllegalArgumentException("Unsupported state: " + state);
        }
    }

    private void setRunning(Set<Node> nodes) {
        for (Node node : nodes) {
            setState(node, RUNNING);
        }
    }

    /**
     * Node states.
     */
    enum State {

        /**
         * Ready.
         */
        READY,

        /**
         * Running.
         */
        RUNNING,

        /**
         * Finished.
         */
        FINISHED
    }
}
