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

import net.stargraph.StarGraphException;
import net.stargraph.data.DataSource;

import java.util.Arrays;
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
        // Attention: when a large HDT file is added to a model (even if it is an empty default model), it takes forever.
        // If a single HDT file is the only data source, one may consider directly returning the HDT model, but then extending the model (model.add()) does not work.

        try {
            JModel mergedModel =  new JModel();
            dataSources.forEach(s -> s.createIterator().forEachRemaining(m -> mergedModel.add(m)));

            return mergedModel;
        } catch (Exception e) {
            throw new StarGraphException(e);
        }
    }
}
