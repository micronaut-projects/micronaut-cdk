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

import io.micronaut.cdk.util.Assert;
import io.micronaut.core.annotation.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Container for {@link Command}s.
 */
public class Commands implements Iterable<Command> {

    private final Collection<Command> commands;

    /**
     * Constructor.
     *
     * @param commands commands
     */
    public Commands(Command... commands) {
        this.commands = Arrays.asList(Assert.notNull(commands, "commands cannot be null"));
    }

    /**
     * Constructor.
     *
     * @param commands commands
     */
    public Commands(@NonNull Collection<Command> commands) {
        this.commands = Assert.notNull(commands, "commands cannot be null");
    }

    /**
     * The commands.
     *
     * @return the commands
     */
    @NonNull
    public Collection<Command> getCommands() {
        return Collections.unmodifiableCollection(commands);
    }

    @NonNull
    @Override
    public Iterator<Command> iterator() {
        return commands.iterator();
    }

    /**
     * Stream.
     *
     * @return the commands as a stream
     */
    @NonNull
    public Stream<Command> stream() {
        return commands.stream();
    }

    @Override
    public String toString() {
        return "Commands{commands=" + commands + '}';
    }
}
