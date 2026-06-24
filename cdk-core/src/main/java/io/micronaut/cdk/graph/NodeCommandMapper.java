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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains bidirectional mapping between Node and Command.
 *
 * @deprecated This class should not be used to maintain the relation between {@link Node} and {@link Command}.
 * The {@link Node} class references its {@link Command} directly, making this mapping unnecessary.
 */
@Deprecated(forRemoval = true)
public class NodeCommandMapper {

    private final Map<Node, Command> commandByNode = new ConcurrentHashMap<>();
    private final Map<Command, Node> nodeByCommand = new ConcurrentHashMap<>();

    /**
     * Constructor.
     */
    public NodeCommandMapper() {
    }

    /**
     * Add a node/command pair.
     *
     * @param node    node
     * @param command command
     */
    public void map(Node node, Command command) {
        commandByNode.put(node, command);
        nodeByCommand.put(command, node);
    }

    /**
     * Get the command for the node.
     *
     * @param node node
     * @return command
     */
    public Command getCommand(Node node) {
        return commandByNode.get(node);
    }

    /**
     * Get the node for the command.
     *
     * @param command command
     * @return node
     */
    public Node getNode(Command command) {
        return nodeByCommand.get(command);
    }
}
