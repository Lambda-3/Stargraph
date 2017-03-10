package net.stargraph.data;

/*-
 * ==========================License-Start=============================
 * stargraph-model
 * --------------------------------------------------------------------
 * Copyright (C) 2017 Lambda^3
 * --------------------------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ==========================License-End===============================
 */

import net.stargraph.data.processor.Holder;
import net.stargraph.model.KBId;

import java.io.Serializable;
import java.util.Objects;

/**
 * Holder of consumable data.
 */
public final class Indexable implements Holder<Serializable> {

    private Serializable data;
    private KBId kbId;
    private boolean sink;

    /**
     * Constructs a indexable data with useful information about its origin (source).
     *
     * @param data   The piece of serializable data to be indexed.
     * @param kbId Identifies the origin of data.
     */
    public Indexable(Serializable data, KBId kbId) {
        set(data);
        if (kbId == null) {
            throw new IllegalArgumentException("kbId can't null.");
        }

        this.data = data;
        this.kbId = kbId;
        this.sink = false;
    }

    @Override
    public String toString() {
        return "Indexable{" +
                "data=" + data.toString() +
                ", where=" + kbId +
                '}';
    }

    @Override
    public KBId getKBId() {
        return kbId;
    }


    @Override
    public Serializable get() {
        return data;
    }

    @Override
    public void set(Serializable data) {
        if (data == null) {
            throw new IllegalArgumentException("Indexable data can't null.");
        }
        if (isSinkable()) {
            throw new IllegalStateException("Modifying state of a sinkable is forbidden");
        }
        this.data = data;
    }

    @Override
    public void setSink(boolean sink) {
        this.sink = sink;
    }

    @Override
    public boolean isSinkable() {
        return sink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Indexable indexable = (Indexable) o;
        return Objects.equals(data, indexable.data) &&
                Objects.equals(kbId, indexable.kbId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, kbId);
    }
}
