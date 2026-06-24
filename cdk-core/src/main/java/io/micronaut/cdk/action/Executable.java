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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

/**
 * Base class for executable actions.
 *
 * @param <E> the executable type
 */
public abstract class Executable<E extends Executable<E>> extends Action<E> {

    private final Collection<IpAddressReference> ipAddressReferences;

    /**
     * Constructor.
     *
     * @param cdkId               CDK ID
     * @param stacks              Stacks
     * @param ipAddressReferences IP address references
     */
    protected Executable(@NonNull String cdkId,
                         @NonNull Collection<String> stacks,
                         @NonNull Collection<IpAddressReference> ipAddressReferences) {
        super(cdkId, stacks);
        this.ipAddressReferences = Assert.notNull(ipAddressReferences, "ipAddressReferences cannot be null");
    }

    /**
     * Run the command(s).
     *
     * @param ec the execution context
     * @return a Result for each command
     */
    public abstract List<Result> execute(@NonNull ExecutionContext ec);

    /**
     * IP address references.
     *
     * @return the references
     */
    @NonNull
    public Collection<IpAddressReference> getIpAddressReferences() {
        return Collections.unmodifiableCollection(ipAddressReferences);
    }

    @Override
    public String toString() {
        return super.toString() +
                ", ipAddressReferences=" + ipAddressReferences;
    }

    /**
     * Create a map between IpAddressReferences and stored IP addresses; keys
     * are variable names and values are IP addresses.
     *
     * @param ec the execution context
     * @return the map
     */
    @NonNull
    protected Map<String, String> resolveIpAddresses(@NonNull ExecutionContext ec) {

        Map<String, String> addressesByVariable = new HashMap<>();

        for (IpAddressReference reference : getIpAddressReferences()) {
            String ipAddress;
            if (reference.isPrivateAddress()) {
                ipAddress = ec.getPrivateIpAddress(reference.getCdkId());
            } else {
                ipAddress = ec.getPublicIpAddress(reference.getCdkId());
            }
            if (ipAddress == null) {
                String privateOrPublic = reference.isPrivateAddress() ? "private" : "public";
                throw new IllegalStateException("No " + privateOrPublic +
                        " IP address available for CDK ID '" + reference.getCdkId() + "'");
            }
            addressesByVariable.put(reference.getVariable(), ipAddress);
        }

        return addressesByVariable;
    }

    /**
     * Replace any {{variable_name}} placeholders with corresponding IP addresses.
     *
     * @param s                   the string possibly containing variable references
     * @param resolvedIpAddresses the map of variables to addresses
     * @return the string with replaced variables
     */
    @NonNull
    protected String resolveIpAddresses(@NonNull String s,
                                        @NonNull Map<String, String> resolvedIpAddresses) {
        Assert.notNull(s, "string cannot be null");

        for (Map.Entry<String, String> entry : resolvedIpAddresses.entrySet()) {
            String variable = entry.getKey();
            String ipAddress = entry.getValue();
            s = s.replaceAll("\\{\\{" + variable + "}}", ipAddress);
        }
        return s;
    }

    /**
     * Resolves variables in a command.
     *
     * @param variables the variables
     * @param command   the command
     * @return the command with variables resolved
     */
    @NonNull
    protected String resolveVariables(@NonNull Map<String, String> variables,
                                      @NonNull String command) {
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            command = command.replaceAll(Matcher.quoteReplacement("$" + entry.getKey()), entry.getValue());
        }
        return command;
    }

    /**
     * Builder.
     *
     * @param <E> the action type
     * @param <B> the builder type
     */
    public abstract static class Builder<E extends Executable<E>, B extends Builder<E, B>> extends Action.Builder<E, B> {

        private final Collection<IpAddressReference> ipAddressReferences = new ArrayList<>();

        /**
         * Constructor.
         *
         * @param cdkId CDK ID
         */
        protected Builder(@NonNull String cdkId) {
            super(cdkId);
        }

        /**
         * Register a new IP address reference. The when a spec for the specified
         * CDK ID runs, it will register any public and private keys with
         * ExecutionContext.registerIpAddress() and later the addresses can be
         * retrieved with the CDK ID.
         *
         * @param variable       the variable name to replace with the IP address
         * @param cdkId          the CDK ID
         * @param privateAddress true for the private IP address, false for public
         * @return this
         */
        @NonNull
        public B ipReference(@NonNull String variable,
                             @NonNull String cdkId,
                             boolean privateAddress) {

            Optional<IpAddressReference> existing = ipAddressReferences.stream()
                    .filter(it -> it.getVariable().equals(variable))
                    .findAny();
            if (existing.isPresent()) {
                throw new IllegalStateException("Existing IpAddressReference for variable '" +
                        variable + "': " + existing);
            }

            ipAddressReferences.add(new IpAddressReference(cdkId, privateAddress, variable));
            return self();
        }

        /**
         * IP address references.
         *
         * @return the references
         */
        @NonNull
        public Collection<IpAddressReference> getIpAddressReferences() {
            return Collections.unmodifiableCollection(ipAddressReferences);
        }
    }

    /**
     * Contains stdout, stderr, exit code, and exception (if one occurred) from
     * running an Executable's command.
     */
    public static class Result {

        private final String command;
        private final String error;
        private final Exception exception;
        private final Integer exitCode;
        private final String output;

        /**
         * Constructor.
         *
         * @param command   the executed command
         * @param error     error output
         * @param exception exception
         * @param exitCode  process exit code, if applicable
         * @param output    process output
         */
        public Result(@NonNull String command,
                      @NonNull String error,
                      @Nullable Exception exception,
                      @Nullable Integer exitCode,
                      @NonNull String output) {
            this.command = command.trim();
            this.error = error.trim();
            this.exception = exception;
            this.exitCode = exitCode;
            this.output = output.trim();
        }

        /**
         * Command.
         *
         * @return the command
         */
        @NonNull
        public String getCommand() {
            return command;
        }

        /**
         * Stderr.
         *
         * @return the stderr
         */
        @NonNull
        public String getError() {
            return error;
        }

        /**
         * The exception if one occurred.
         *
         * @return the exception
         */
        @Nullable
        public Exception getException() {
            return exception;
        }

        /**
         * Exit code.
         *
         * @return the exit code
         */
        @Nullable
        public Integer getExitCode() {
            return exitCode;
        }

        /**
         * Stdout.
         *
         * @return the stdout
         */
        @NonNull
        public String getOutput() {
            return output;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "command='" + command + '\'' +
                    ", error='" + error + '\'' +
                    ", exception=" + exception +
                    ", exitCode=" + exitCode +
                    ", output='" + output + '\'' +
                    '}';
        }
    }
}
