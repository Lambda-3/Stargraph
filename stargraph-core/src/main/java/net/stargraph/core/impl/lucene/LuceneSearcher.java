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
import net.stargraph.core.Stargraph;
import net.stargraph.core.search.BaseSearcher;
import net.stargraph.core.search.SearchQueryHolder;
import net.stargraph.model.KBId;
import net.stargraph.rank.Scores;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.Objects;

public final class LuceneSearcher extends BaseSearcher {
    private Directory directory;
    private IndexReader indexReader;
    private IndexSearcher indexSearcher;

    public LuceneSearcher(KBId kbId, Stargraph core, Directory directory) {
        super(kbId, core);
        this.directory = Objects.requireNonNull(directory);
    }

    @Override
    public Scores search(SearchQueryHolder holder) {
        //TODO implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public long countDocuments() {
        IndexSearcher idxSearcher = getLuceneSearcher();
        if (idxSearcher != null) {
            return idxSearcher.getIndexReader().numDocs();
        }
        throw new StarGraphException("Index not found for " + kbId);
    }


    @Override
    protected void onStop() {
        try {
            if (indexReader != null) {
                indexReader.close();
                indexReader = null;
            }
        } catch (IOException e) {
            throw new StarGraphException(e);
        }
    }

    private synchronized IndexSearcher getLuceneSearcher() {
        try {
            if (indexReader == null) {
                if (!DirectoryReader.indexExists(directory)) {
                    return null;
                }
                indexReader = DirectoryReader.open(directory);
                indexSearcher = new IndexSearcher(indexReader);
            }
            return indexSearcher;
        }
        catch (IOException e) {
            throw new StarGraphException(e);
        }
    }
}


