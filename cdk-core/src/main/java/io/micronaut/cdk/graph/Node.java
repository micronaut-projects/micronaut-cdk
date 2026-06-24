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

import io.micronaut.cdk.action.Action;
import io.micronaut.cdk.command.Command;
import io.micronaut.cdk.util.Assert;
import io.micronaut.core.annotation.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Simple class representing a node in a graph.
 */
public class Node {

    /**
     * Nodes that must complete before this node can execute, incoming edges.
     */
    private final Set<Node> predecessors = new HashSet<>();

    /**
     * Nodes that require this node to complete before they can execute, outgoing edges.
     */
    private final Set<Node> successors = new HashSet<>();

    private int inDegree;

    private int outDegree;

    private final Command command;

    /**
     * Constructor.
     *
     * @param command the command
     */
    public Node(@NonNull Command command) {
        this.command = Assert.notNull(command, "command cannot be null");
    }

    /**
     * @return the command
     */
    public Command getCommand() {
        return command;
    }

    /**
     * The action.
     *
     * @return the action
     */
    @NonNull
    public Action<?> getAction() {
        return command.getAction();
    }

    /**
     * Add a predecessor to this node.
     * <p>
     * Note: This method is intended to be used only through the {@link Graph} API.
     *
     * @param predecessor the predecessor node
     */
    void addPredecessor(@NonNull Node predecessor) {
        Assert.notNull(predecessor, "predecessor cannot be null");
        if (predecessors.add(predecessor)) {
            inDegree++;
        }
    }

    /**
     * Add a successor to this node.
     * <p>
     * Note: This method is intended to be used only through the {@link Graph} API.
     *
     * @param successor the successor node
     */
    void addSuccessor(@NonNull Node successor) {
        Assert.notNull(successor, "successor cannot be null");
        if (successors.add(successor)) {
            outDegree++;
        }
    }

    /**
     * @return dependencies of this node
     */
    @NonNull
    public Collection<Node> getPredecessors() {
        return Collections.unmodifiableCollection(predecessors);
    }

    /**
     * @return dependents of this node
     */
    @NonNull
    public Collection<Node> getSuccessors() {
        return Collections.unmodifiableCollection(successors);
    }

    /**
     * @return number of dependencies of this node.
     */
    public int getInDegree() {
        return inDegree;
    }

    /**
     * @return number of dependents of this node.
     */
    public int getOutDegree() {
        return outDegree;
    }

    // TODO two nodes can be considered equal although they use a different Action - i.e. DeleteSpec + Spec are deemed equal for the same CDK ID
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var node = (Node) o;
        return Objects.equals(command, node.getCommand());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAction().getCdkId());
    }

    @Override
    public String toString() {
        return "Node{command='" + command + "'}";
    }
}
