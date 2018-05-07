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

import jersey.repackaged.com.google.common.collect.Iterators;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Primary data consumer interface.
 */
public class DataProvider<T> {
    private List<DataSource<T>> dataSources;

    public DataProvider(DataSource<T> dataSource) {
        this(Arrays.asList(dataSource));
    }

    public DataProvider(List<DataSource<T>> dataSources) {
        this.dataSources = Objects.requireNonNull(dataSources);
    }

    public Stream<T> getStream() {
        return StreamSupport.stream(this.getSpliterator(), false);
    }

    public Iterator<T> getIterator() {
        return Iterators.concat(dataSources.stream().map(s -> s.getIterator()).iterator());
    }

    public Spliterator<T> getSpliterator() {
        return Spliterators.spliteratorUnknownSize(getIterator(), Spliterator.NONNULL);
    }

}
