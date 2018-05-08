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
import net.stargraph.StarGraphException;
import net.stargraph.model.GraphModel;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Primary data generating interface.
 */
public class DataProvider<T> {
    private List<DataSource<T>> dataSources;
    private DataGenerator<? extends GraphModel, T> graphModelUpdater; // optional, can be null

    public DataProvider(DataSource<T> dataSource) {
        this(Arrays.asList(dataSource));
    }

    public DataProvider(List<DataSource<T>> dataSources) {
        this(dataSources, null);
    }

    public DataProvider(List<DataSource<T>> dataSources, DataGenerator<? extends GraphModel, T> graphModelUpdater) {
        this.dataSources = Objects.requireNonNull(dataSources);
        this.graphModelUpdater = graphModelUpdater;
    }



    public List<DataSource<T>> getDataSources() {
        return dataSources;
    }

    public DataSource<T> getMergedDataSource() {
        return new DataSource<T>() {
            @Override
            public Iterator<T> getIterator() {
                return Iterators.concat(dataSources.stream().map(s -> s.getIterator()).iterator());
            }
        };
    }

    public boolean hasGraphModelUpdater() {
        return graphModelUpdater != null;
    }

    public DataGenerator<? extends GraphModel, T> getGraphModelUpdater() {
        if (graphModelUpdater != null) {
            return graphModelUpdater;
        } else {
            throw new StarGraphException("No graph model updater exists.");
        }
    }

}
