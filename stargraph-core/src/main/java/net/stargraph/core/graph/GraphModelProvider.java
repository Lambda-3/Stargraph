package net.stargraph.core.graph;

/*-
 * ==========================License-Start=============================
 * stargraph-core
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

import net.stargraph.data.DataSource;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class GraphModelProvider {
    private List<DataSource<JModel>> dataSources;

    public GraphModelProvider(DataSource<JModel> dataSource) {
        this(Arrays.asList(dataSource));
    }

    public GraphModelProvider(List<DataSource<JModel>> dataSources) {
        this.dataSources = Objects.requireNonNull(dataSources);
    }

    public JModel getGraphModel() {
        JModel mergedModel = null;

        // adding large graph models (even to an empty "createDefaultModel") is very expensive,
        // therefore avoid model.add() and avoid using multiple data sources for a graph model
        for (DataSource<JModel> dataSource : dataSources) {
            for (Iterator<JModel> iterator = dataSource.getIterator(); iterator.hasNext(); ) {
                JModel model = iterator.next();
                if (mergedModel == null) {
                    mergedModel = model;
                } else {
                    mergedModel.add(model);
                }
            }
        }

        if (mergedModel == null) {
            mergedModel = new JModel();
        }

        return mergedModel;
    }
}
