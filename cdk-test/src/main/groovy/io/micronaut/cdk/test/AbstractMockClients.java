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
package io.micronaut.cdk.test;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Base class for mock clients implementations.
 */
public abstract class AbstractMockClients {

    /**
     * Create a JDK proxy that throws an exception for every method call if the
     * client was null so nulls aren't returned from @NonNull methods.
     *
     * @param client          the possibly null client
     * @param clientInterface the client interface
     * @param <T>             the interface type
     * @return the client if not null, or a proxy otherwise
     */
    @NonNull
    protected <T> T proxyIfNull(@Nullable T client,
                                @NonNull Class<T> clientInterface) {
        return client == null ? proxy(Objects.requireNonNull(clientInterface)) : client;
    }

    /**
     * Create a JDK proxy that throws an exception for every method call.
     *
     * @param iface the interface
     * @param <T>   the interface type
     * @return a proxy
     */
    @NonNull
    @SuppressWarnings("unchecked")
    protected <T> T proxy(@NonNull Class<T> iface) {
        Objects.requireNonNull(iface);

        InvocationHandler invocationHandler = (proxy, m, args) -> {
            if (m.getName().equals("toString")) {
                return "Mock for interface " + iface.getName();
            }
            throw new UnsupportedOperationException();
        };

        return (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class[]{iface},
                invocationHandler);
    }

    /**
     * Base class for mock clients builders.
     *
     * @param <CLIENTS> the clients holder type
     */
    public abstract static class Builder<CLIENTS> {

        /**
         * Clients keyed by interface.
         */
        protected final Map<Class<?>, Object> clients = new HashMap<>();

        /**
         * Register a new client.
         *
         * @param clientInterface the client interface
         * @param client          the client
         * @param <C>             the client type
         * @return this
         */
        public <C> Builder<CLIENTS> client(Class<C> clientInterface, C client) {
            if (!clientInterface.isInterface()) {
                throw new IllegalStateException("First arg must be an interface, not a class, e.g. Compute instead of ComputeClient");
            }
            clients.put(clientInterface, client);
            return this;
        }

        /**
         * @return the finished instance
         */
        public abstract CLIENTS build();
    }
}
