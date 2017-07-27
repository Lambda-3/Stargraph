package net.stargraph.test.it;

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

import com.typesafe.config.ConfigFactory;
import net.stargraph.ModelUtils;
import net.stargraph.core.Stargraph;
import net.stargraph.core.impl.elastic.ElasticIndexer;
import net.stargraph.core.impl.elastic.ElasticSearcher;
import net.stargraph.core.index.Indexer;
import net.stargraph.data.Indexable;
import net.stargraph.model.Fact;
import net.stargraph.model.KBId;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Aims to test the incremental indexing features.
 */
public final class IndexUpdateIT {

    private Stargraph core;
    private Indexer indexer;
    private ElasticSearcher searcher;
    private KBId kbId = KBId.of("simple", "facts");

    @BeforeClass
    public void before() throws Exception {
        ConfigFactory.invalidateCaches();
        core = new Stargraph();
        searcher = new ElasticSearcher(kbId, core);
        searcher.start();
        indexer = new ElasticIndexer(kbId, core);
        indexer.start();
        indexer.deleteAll();
    }

    @Test
    public void updateTest() throws InterruptedException, TimeoutException, ExecutionException {
        Fact oneFact = ModelUtils.createFact(kbId, "dbr:Barack_Obama", "dbp:spouse", "dbr:Michelle_Obama");
        indexer.index(new Indexable(oneFact, kbId));
        indexer.flush();
        Assert.assertEquals(searcher.countDocuments(), 1);
    }
}
