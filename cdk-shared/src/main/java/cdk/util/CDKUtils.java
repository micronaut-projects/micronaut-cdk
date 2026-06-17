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
package cdk.util;

import io.micronaut.core.annotation.NonNull;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

import static ch.qos.logback.classic.Level.ALL_INT;
import static ch.qos.logback.classic.Level.DEBUG_INT;
import static ch.qos.logback.classic.Level.ERROR_INT;
import static ch.qos.logback.classic.Level.INFO_INT;
import static ch.qos.logback.classic.Level.OFF_INT;
import static ch.qos.logback.classic.Level.TRACE_INT;
import static ch.qos.logback.classic.Level.WARN_INT;
import static java.util.logging.Level.ALL;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.OFF;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

/**
 * Utility methods.
 */
public final class CDKUtils {

    private static final Map<Integer, java.util.logging.Level> LOGBACK_TO_JUL = Map.of(
            OFF_INT, OFF,
            ERROR_INT, SEVERE,
            WARN_INT, WARNING,
            INFO_INT, INFO,
            DEBUG_INT, FINE,
            TRACE_INT, FINEST,
            ALL_INT, ALL);

    private CDKUtils() {
        // static only
    }

    /**
     * Configure the java.util.logging bridge.
     */
    public static void configureLogging() {
        SLF4JBridgeHandler.removeHandlersForRootLogger(); // remove existing handlers attached to j.u.l root logger
        SLF4JBridgeHandler.install();                     // add SLF4JBridgeHandler to j.u.l's root logger
        java.util.logging.Logger.getLogger("").setLevel(julRootLevel()); // root logger level
    }

    /**
     * Replace a starting ~ char with the user home.
     *
     * @param path a file path
     * @return the path with ~ replaced
     */
    public static String resolveTilde(@NonNull String path) {
        path = Objects.requireNonNull(path).trim();
        if (path.startsWith("~")) {
            path = System.getProperty("user.home") + path.substring(1);
        }
        return path;
    }

    /**
     * Escape a variable name to be replaced with the value once it's available.
     *
     * @param variableName the name
     * @return the escaped variable
     */
    @NonNull
    public static String escapeVariable(@NonNull String variableName) {
        return "{{" + Objects.requireNonNull(variableName) + "}}";
    }

    /**
     * Read the contents of a text file.
     *
     * @param path the file path
     * @return the contents
     * @throws IOException if there's a problem
     */
    @NonNull
    public static String readTextFile(@NonNull String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(CDKUtils.resolveTilde(path)))).trim();
    }

    private static java.util.logging.Level julRootLevel() {
        ILoggerFactory factory = LoggerFactory.getILoggerFactory();

        if (factory instanceof ch.qos.logback.classic.LoggerContext ctx) {
            ch.qos.logback.classic.Logger rootLogger = ctx.getLogger(ROOT_LOGGER_NAME);
            ch.qos.logback.classic.Level level = rootLogger.getLevel();
            if (level == null) {
                level = ch.qos.logback.classic.Level.INFO; // defensive
            }

            var mapped = LOGBACK_TO_JUL.get(level.toInt());
            if (mapped == null) {
                throw new IllegalStateException("Unexpected root logger level: " + level);
            }
            return mapped;
        }
        return INFO;
    }

}
