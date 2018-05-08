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

import net.stargraph.core.Stargraph;
import net.stargraph.core.data.FileDataSource;
import net.stargraph.core.graph.BaseGraphModelProviderFactory;
import net.stargraph.core.graph.GraphModelProvider;
import net.stargraph.model.KBId;

import java.io.File;
import java.util.Iterator;

public final class NTriplesModelProviderFactory extends BaseGraphModelProviderFactory {

    public NTriplesModelProviderFactory(Stargraph stargraph) {
        super(stargraph);
    }

    @Override
    public GraphModelProvider create(String dbId) {
        final KBId kbId = KBId.of(dbId, "facts");

        return new GraphModelProvider(
                new FileDataSource(stargraph, kbId, "triples.nt", "triples.nt") {
                    @Override
                    protected Iterator getIterator(Stargraph stargraph, KBId kbId, File file) {
                        return new NTriplesModelFileLoader(kbId.getId(), file).loadModelAsIterator();
                    }
                }
        );
    }
}
