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
import io.micronaut.cdk.action.GroupAction;
import io.micronaut.cdk.command.Command;
import io.micronaut.cdk.command.Commands;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds a directed acyclic graph (DAG) from a set of commands and their dependencies.
 */
public class GraphBuilder {

    private final Commands commands;

    /**
     * Constructor.
     *
     * @param commands the commands
     */
    public GraphBuilder(Commands commands) {
        this.commands = commands;
    }

    /**
     * Processes group nodes in the graph, expanding dependencies in and out the group.
     * Each action in the group gets the enclosing Group as a predecessor: actions in group start only after the group
     * is processed.
     * Each action that is a successor of ANY action contained in the group will get all group's actions as its
     * predecessors: will only run after all actions in Group complete.
     * Each action that is a predecessor of ANY action contained in the group will become a predecessor of the Group
     * action: no action in the group executes unless all predecessors complete.
     *
     * @param graph graph to process.
     */
    void processGroupNodes(Graph graph) {
        for (Node grpNode : graph.getNodes()) {
            if (grpNode.getAction() instanceof GroupAction<?> grp) {
                for (Action<?> child : grp.getContents()) {
                    Node childNode = graph.getNode(child);
                    if (childNode == null) {
                        throw new IllegalStateException("Action " + child + " contained in " + grp + " is has no command.");
                    }
                }
                // TODO handle nested groups
                Node prevNode = null;
                for (Action<?> child : grp.getContents()) {
                    Node childNode = graph.getNode(child);
                    if (grp.isUnit()) {
                        for (Node preGroupNode : childNode.getPredecessors()) {
                            if (!grp.getContents().contains(preGroupNode.getAction())) {
                                graph.addDependency(grpNode, preGroupNode);
                            }
                        }
                        graph.addDependency(childNode, grpNode);
                        for (Node postGroupNode : childNode.getSuccessors()) {
                            if (!grp.getContents().contains(postGroupNode.getAction())) {
                                for (Action<?> a : grp.getContents()) {
                                    graph.addDependency(postGroupNode, graph.getNode(a));
                                }
                            }
                        }
                    }
                    if (grp.isOrdered() && prevNode != null) {
                        graph.addDependency(childNode, prevNode);
                    }
                    prevNode = childNode;
                }
            }
        }
    }

    /**
     * Construct the graph.
     *
     * @return the constructed graph
     * @throws DependencyCycleException if there's a cycle
     */
    public Graph build() {
        Graph graph = new Graph();
        Map<Action<?>, Node> actionNodeMap = new HashMap<>();

        for (Command cmd : commands) {
            Action<?> action = cmd.getAction();
            if (actionNodeMap.containsKey(action)) {
                throw new IllegalStateException(
                        String.format("Duplicate action detected: Action '%s' must be unique.", action)
                );
            }

            Node node = new Node(cmd);
            graph.addNode(node);
            actionNodeMap.put(action, node);
        }

        for (Node node1 : actionNodeMap.values()) {
            Action<?> action = node1.getAction();
            for (Action<?> dep : action.getDependencies()) {
                Node node2 = actionNodeMap.get(dep);
                graph.addDependency(node1, node2);
            }
        }
        processGroupNodes(graph);
        graph.ensureNoCycle();

        return graph;
    }

}
