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
package io.micronaut.cdk.util;

import io.micronaut.core.annotation.NonNull;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Distribute output to multiple output streams.
 */
public class MultiOutputStream extends FilterOutputStream {

    private final List<OutputStream> streams = new ArrayList<>();

    private boolean preventCloseOthers;

    /**
     * Creates an output stream filter built on top of the specified
     * underlying output stream.
     *
     * @param out    the underlying output stream to be assigned to
     *               the field {@code this.out} for later use, or
     *               <code>null</code> if this instance is to be
     *               created without an underlying stream
     * @param others other output streams
     */
    public MultiOutputStream(OutputStream out,
                             OutputStream... others) {
        this(false, out, others);
    }

    /**
     * Creates an output stream filter built on top of the specified
     * underlying output stream.
     *
     * @param preventCloseOthers whether to prevent closing other streams
     * @param out                the underlying output stream to be assigned to
     *                           the field {@code this.out} for later use, or
     *                           <code>null</code> if this instance is to be
     *                           created without an underlying stream
     * @param others             other output streams
     */
    public MultiOutputStream(boolean preventCloseOthers,
                             OutputStream out,
                             OutputStream... others) {
        super(out);
        streams.addAll(Arrays.asList(others));
        this.preventCloseOthers = preventCloseOthers;
    }

    /**
     * Adds an output stream.
     *
     * @param out the stream
     */
    public void addOutputStream(OutputStream out) {
        streams.add(out);
    }

    /**
     * Whether to prevent closing other streams.
     *
     * @return true if preventing closing other streams
     */
    public boolean isPreventCloseOthers() {
        return preventCloseOthers;
    }

    /**
     * Sets prevent closing other streams.
     *
     * @param preventCloseOthers true to prevent closing
     */
    public void setPreventCloseOthers(boolean preventCloseOthers) {
        this.preventCloseOthers = preventCloseOthers;
    }

    /**
     * Writes the specified <code>byte</code> to all output streams.
     *
     * @param b the <code>byte</code>
     * @throws IOException if an I/O error occurs
     */
    @Override
    public synchronized void write(int b) throws IOException {
        super.write(b);
        for (OutputStream out : streams) {
            out.write(b);
        }
    }

    /**
     * Writes <code>len</code> bytes from the specified
     * <code>byte</code> array starting at offset <code>off</code> to
     * all output streams.
     *
     * @param data   the data
     * @param offset the start offset in the data
     * @param length the number of bytes to write
     * @throws IOException if an I/O error occurs.
     * @see java.io.FilterOutputStream#write(int)
     */
    @Override
    public synchronized void write(@NonNull byte[] data, int offset, int length) throws IOException {
        Assert.notNull(data, "data cannot be null");

        super.write(data, offset, length);
        for (OutputStream out : streams) {
            out.write(data, offset, length);
        }
    }

    /**
     * Flushes all output streams.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void flush() throws IOException {
        super.flush();
        for (OutputStream out : streams) {
            out.flush();
        }
    }

    /**
     * Closes the output stream, and the other streams if <code>preventCloseOthers</code> is false.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        super.close();
        if (!preventCloseOthers) {
            for (OutputStream out : streams) {
                out.close();
            }
        }
    }
}
