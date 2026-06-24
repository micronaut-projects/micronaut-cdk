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
package io.micronaut.cdk.action;

import io.micronaut.cdk.ExecutionContext;
import io.micronaut.cdk.component.IpAddressReference;
import io.micronaut.cdk.util.Assert;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents executable processes (scripts, apps, etc.).
 *
 * @param <S> the type
 */
public class Script<S extends Script<S>> extends Executable<S> {

    private final List<String> command;
    private final File directory;
    private final Map<String, String> environment;

    Script(@NonNull String cdkId,
           @NonNull Collection<String> stacks,
           @NonNull Collection<IpAddressReference> ipAddressReferences,
           @NonNull List<String> command,
           @Nullable File directory,
           @NonNull Map<String, String> environment) {
        super(cdkId, stacks, ipAddressReferences);
        this.command = Assert.notNull(command, "command cannot be null");
        this.directory = directory;
        this.environment = Assert.notNull(environment, "environment cannot be null");
    }

    /**
     * The command and args.
     *
     * @return the command and args
     */
    @NonNull
    public List<String> getCommand() {
        return command;
    }

    /**
     * The working directory.
     *
     * @return the working directory
     */
    @Nullable
    public File getDirectory() {
        return directory;
    }

    /**
     * Environment.
     *
     * @return the environment
     */
    @NonNull
    public Map<String, String> getEnvironment() {
        return environment;
    }

    @Override
    public List<Result> execute(@NonNull ExecutionContext ec) {
        Assert.notNull(ec, "ExecutionContext cannot be null");

        ProcessBuilder pb = new ProcessBuilder();
        pb.command(command);
        pb.directory(directory);
        pb.environment().putAll(environment);
        pb.redirectErrorStream(true);

        Exception exception = null;
        Integer exitCode = null;
        String output = "";
        try {
            Process process = pb.start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                output = br.lines().collect(Collectors.joining("\n"));
            }
            exitCode = process.waitFor();
        } catch (Exception e) {
            exception = e;
        }

        return List.of(new Result(String.join(" ", command), "", exception, exitCode, output));
    }

    @Override
    public String toString() {
        return "Script{" +
                "command=" + command +
                ", directory=" + directory +
                ", environment=" + environment +
                '}';
    }

    /**
     * Builder.
     *
     * @param cdkId the CDK ID
     * @param <S>   the Script type
     * @param <B>   the builder type
     * @return a new builder
     */
    @NonNull
    public static <S extends Script<S>, B extends Builder<S, B>> Builder<S, B> builder(
            @NonNull String cdkId) {
        return new Builder<>(cdkId);
    }

    /**
     * Builder.
     *
     * @param <S> the Script type
     * @param <B> the builder type
     */
    public static class Builder<S extends Script<S>, B extends Builder<S, B>> extends Executable.Builder<S, B> {

        private final List<String> commands = new ArrayList<>();
        private final Map<String, String> environment = new HashMap<>();

        private File directory;

        Builder(@NonNull String cdkId) {
            super(cdkId);
        }

        /**
         * Add commands.
         *
         * @param commands command and/or args to run
         * @return this
         */
        @NonNull
        public B commands(@NonNull List<String> commands) {
            this.commands.addAll(commands);
            return self();
        }

        /**
         * Add a command.
         *
         * @param command command to run or arg
         * @return this
         */
        @NonNull
        public B command(@NonNull String command) {
            commands.add(command);
            return self();
        }

        /**
         * Add an environment variable.
         *
         * @param name  the name
         * @param value the value
         * @return this
         */
        @NonNull
        public B env(@NonNull String name,
                     @NonNull String value) {
            environment.put(name, value);
            return self();
        }

        /**
         * The working directory.
         *
         * @param directory the working directory
         * @return this
         */
        @NonNull
        public B directory(@NonNull File directory) {
            this.directory = directory;
            return self();
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        protected S doBuild() throws InvalidActionException {
            return (S) new Script<>(
                    getCdkId(),
                    getStacks(),
                    getIpAddressReferences(),
                    commands,
                    directory,
                    environment);
        }

        @Override
        protected void validate() throws InvalidActionException {
            super.validate();

            Assert.state(!commands.isEmpty(), "At least one command is required");

            if (directory != null) {
                Assert.exists(directory);
            }
        }
    }
}
