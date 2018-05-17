package net.stargraph.core.data;

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
import net.stargraph.data.DataProvider;
import net.stargraph.data.Indexable;
import net.stargraph.model.KBId;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Encapsulates the logic to provide a stream of documents.
 */
public final class DocumentProviderFactory extends BaseDataProviderFactory {

    public DocumentProviderFactory(Stargraph stargraph) {
        super(stargraph);
    }

    @Override
    public DataProvider<Indexable> create(KBId kbId) {
        return new DataProvider<Indexable>(
                Arrays.asList(
                    new FileDataSource(stargraph, kbId, "documents.json", "documents.json", false) {
                        @Override
                        protected Iterator createIterator(Stargraph stargraph, KBId kbId, File file) {
                            return new DocumentFileIterator(stargraph, kbId, file);
                        }
                    }
                )
        );
    }

}
