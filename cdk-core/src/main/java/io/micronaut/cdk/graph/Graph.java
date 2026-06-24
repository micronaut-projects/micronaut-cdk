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
import io.micronaut.cdk.util.Assert;
import io.micronaut.core.annotation.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple graph implementation supporting topological sort and cycle detection.
 * <p>
 * <b>Usage:</b> Construct and configure the graph using {@link GraphBuilder} to manage node dependencies.
 */
public class Graph {

    private final Map<Action<?>, Node> nodes = new LinkedHashMap<>();
    private final Set<Node> initialNodes = new LinkedHashSet<>();

    /**
     * The nodes.
     *
     * @return the nodes
     */
    public Collection<Node> getNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    /**
     * Nodes with no predecessors (can be executed immediately).
     * <p>
     * <b>Note:</b> This set is populated during cycle detection in {@link #hasCycle()},
     * which is called by {@link GraphBuilder#build()} through {@link #ensureNoCycle()}.
     *
     * @return Nodes with no predecessors.
     */
    public Collection<Node> getInitialNodes() {
        return Collections.unmodifiableCollection(initialNodes);
    }

    /**
     * Add node to the graph.
     *
     * @param node node
     */
    void addNode(@NonNull Node node) {
        Assert.notNull(node, "Node cannot be null");
        nodes.putIfAbsent(node.getAction(), node);
    }

    /**
     * Register a dependency between Node 1 and Node 2.
     * Node 1 depends on Node 2 means Node 2 enable Node 1 to be executed.
     *
     * @param node1 Node
     * @param node2 Node
     */
    void addDependency(@NonNull Node node1, @NonNull Node node2) {
        addEdge(node2, node1);
    }

    /**
     * Add an edge.
     *
     * @param source      source node
     * @param destination destination node
     */
    void addEdge(@NonNull Node source, @NonNull Node destination) {
        Assert.notNull(source, "Source node cannot be null");
        Assert.notNull(destination, "Destination node cannot be null");

        Node sourceNode = getNode(source);
        Node destinationNode = getNode(destination);
        if (sourceNode == null || destinationNode == null) {
            throw new IllegalStateException("Source or Destination node not found in the graph");
        }

        sourceNode.addSuccessor(destinationNode);
        destinationNode.addPredecessor(sourceNode);
    }

    /**
     * @param node node
     * @return the node in the graph or null if it doesn't exist.
     */
    public Node getNode(Node node) {
        return getNode(node.getAction());
    }

    /**
     * Returns a Node for the action. Returns {@code null} if the Action is not represented by a Node.
     *
     * @param action the action
     * @return Node or {@code null}.
     */
    Node getNode(Action<?> action) {
        return nodes.get(action);
    }

    /**
     * Sort.
     *
     * @return the nodes in dependency order
     * @throws DependencyCycleException if there's a cycle
     */
    @NonNull
    public List<Node> sort() throws DependencyCycleException {
        ensureNoCycle();

        Set<Node> visited = new HashSet<>();
        List<Node> sorted = new LinkedList<>();

        for (var node : nodes.values()) {
            if (!visited.contains(node)) {
                sort(node, sorted, visited);
            }
        }

        return sorted;
    }

    private void sort(Node node,
                      List<Node> sorted,
                      Set<Node> visited) {

        visited.add(node);

        for (var neighbor : node.getPredecessors()) {
            if (!visited.contains(neighbor)) {
                sort(neighbor, sorted, visited);
            }
        }

        sorted.add(node);
    }

    /**
     * Throws {@code DependencyCycleException} if the graph has a cycle.
     */
    public void ensureNoCycle() {
        // TODO report what nodes/actions form the cycle.
        if (hasCycle()) {
            throw new DependencyCycleException();
        }
    }

    /**
     * Whether the graph has a cycle or not.
     *
     * @return true if it has a cycle
     */
    private boolean hasCycle() {

        Set<Node> visited = new HashSet<>();
        Set<Node> processing = new HashSet<>();

        for (var node : nodes.values()) {
            if (hasCycle(node, visited, processing)) {
                return true;
            }

            if (node.getInDegree() == 0) {
                initialNodes.add(node);
            }
        }

        return false;
    }

    private boolean hasCycle(Node node,
                             Set<Node> visited,
                             Set<Node> processing) {

        if (processing.contains(node)) {
            return true;
        }

        if (visited.contains(node)) {
            return false;
        }

        visited.add(node);
        processing.add(node);

        Collection<Node> nodeEdges = node.getPredecessors();

        for (var c : nodeEdges) {
            if (hasCycle(c, visited, processing)) {
                return true;
            }
        }

        processing.remove(node);

        return false;
    }
}
