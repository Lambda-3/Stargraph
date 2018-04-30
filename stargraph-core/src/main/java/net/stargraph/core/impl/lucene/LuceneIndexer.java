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
import net.stargraph.core.index.BaseIndexer;
import net.stargraph.model.InstanceEntity;
import net.stargraph.model.KBId;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Objects;

public final class LuceneIndexer extends BaseIndexer {
    private Directory directory;
    private IndexWriter writer;
    private IndexWriterConfig writerConfig;

    public LuceneIndexer(KBId kbId, Stargraph stargraph, Directory directory) {
        super(kbId, stargraph);
        this.directory = Objects.requireNonNull(directory);
    }

    @Override
    protected void beforeLoad(boolean reset) {
        if (reset) {
            deleteAll();
        }
    }

    @Override
    protected void doIndex(Serializable data, KBId kbId) throws InterruptedException {
        try {
            writer.addDocument(createDocument(data));
        } catch (IOException e) {
            throw new StarGraphException(e);
        }
    }

    @Override
    protected void doFlush() {
        try {
            writer.flush();
        } catch (IOException e) {
            throw new StarGraphException("Flush error.", e);
        }
    }

    @Override
    protected void afterLoad() throws InterruptedException {
        try {
            writer.commit();
            writer.flush();
            writer.forceMerge(1, true);
        } catch (IOException e) {
           throw new StarGraphException("After loading error.", e);
        }
    }

    @Override
    protected void doDeleteAll() {
        try {
            writer.deleteAll();
        } catch (IOException e) {
            throw new StarGraphException("Delete error.", e);
        }
    }

    @Override
    protected void onStart() {
        try {
            writer = new IndexWriter(directory, getWriterConfig());
        } catch (IOException e) {
            throw new StarGraphException("Fail to initialize the directory.", e);
        }
    }

    @Override
    protected void onStop() {
        try {
            writer.close();
        } catch (IOException e) {
            logger.error("Fail to close index.", e);
        }
    }

    private IndexWriterConfig getWriterConfig() {
        if (writerConfig == null) {
            writerConfig = new IndexWriterConfig(new StandardAnalyzer());
            writerConfig.setCommitOnClose(true);
        }
        return writerConfig;
    }

    private static Document createDocument(Serializable data) {
        final Document doc = new Document();

        //TODO support indexing of other types?
        if (data instanceof InstanceEntity) {
            InstanceEntity entity = (InstanceEntity)data;
            doc.add(new TextField("id", new StringReader(entity.getId())));
            doc.add(new TextField("value", new StringReader(entity.getValue())));
            return doc;
        }

        throw new UnsupportedOperationException("Can't index: " + data.getClass());
    }

}
