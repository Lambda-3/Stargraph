package net.stargraph.core.impl.lucene;

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
import net.stargraph.core.IndicesFactory;
import net.stargraph.core.Stargraph;
import net.stargraph.core.index.BaseIndexer;
import net.stargraph.core.search.BaseSearcher;
import net.stargraph.core.search.SearchQueryGenerator;
import net.stargraph.model.KBId;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LuceneFactory implements IndicesFactory {
    private Map<KBId, Directory> luceneDirs = new ConcurrentHashMap<>();

    @Override
    public BaseIndexer createIndexer(KBId kbId, Stargraph stargraph) {
        return new LuceneIndexer(kbId, stargraph, getLuceneDir(stargraph, kbId));
    }

    @Override
    public BaseSearcher createSearcher(KBId kbId, Stargraph stargraph) {
        return new LuceneSearcher(kbId, stargraph, getLuceneDir(stargraph, kbId));
    }

    @Override
    public SearchQueryGenerator createSearchQueryGenerator(KBId kbId, Stargraph stargraph) {
        return new LuceneSearchQueryGenerator();
    }


    private Directory getLuceneDir(Stargraph stargraph, KBId kbId) {
        return luceneDirs.computeIfAbsent(kbId,
                (id) -> {
                    try {
                        Path idxPath = Paths.get(stargraph.getModelDataDir(kbId), "lucene-store");
                        return new MMapDirectory(idxPath);
                    } catch (IOException e) {
                        throw new StarGraphException(e);
                    }
                });
    }

    //TODO: Put finalization code for each Lucene Directory. Suggestion: terminate method called by Stargraph class?
}
