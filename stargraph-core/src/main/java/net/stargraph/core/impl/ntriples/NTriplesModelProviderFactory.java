package net.stargraph.core.impl.ntriples;

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

import com.typesafe.config.Config;
import net.stargraph.core.Stargraph;
import net.stargraph.core.graph.BaseGraphModelProviderFactory;
import net.stargraph.core.graph.DefaultFileGraphSource;
import net.stargraph.core.graph.GraphModelProvider;

public final class NTriplesModelProviderFactory extends BaseGraphModelProviderFactory {

    public NTriplesModelProviderFactory(Stargraph stargraph) {
        super(stargraph);
    }

    @Override
    public GraphModelProvider create(String dbId) {
        Config config = stargraph.getKBCore(dbId).getConfig();

        final String cfgFilePath = "graphmodel.ntriples.file";
        String resourcePath = "triples.nt";
        if (config.hasPath(cfgFilePath)) {
            resourcePath = config.getString(cfgFilePath);
        }

        final String cfgInMemoryPath = "graphmodel.ntriples.in-memory";
        boolean inMemory = config.hasPath(cfgInMemoryPath)? config.getBoolean(cfgInMemoryPath) : true;

        final String cfgResetPath = "graphmodel.ntriples.reset";
        boolean reset = config.hasPath(cfgResetPath) && config.getBoolean(cfgResetPath);

        return new GraphModelProvider(
                stargraph, dbId, inMemory, reset, new DefaultFileGraphSource(stargraph, dbId, resourcePath, null, true)
        );
    }
}
