package net.stargraph.test;

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
import com.typesafe.config.ConfigFactory;
import net.stargraph.core.Stargraph;
import net.stargraph.core.index.Indexer;
import net.stargraph.core.search.Searcher;
import net.stargraph.model.KBId;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

public final class LuceneIndexerTest {

    private KBId kbId = KBId.of("obama", "entities"); // Entities uses Lucene. See reference.conf.
    private Stargraph core;


    @BeforeClass
    public void beforeClass() {
        ConfigFactory.invalidateCaches();
        Config config = ConfigFactory.load().getConfig("stargraph");
        File dataRootDir = TestUtils.prepareObamaTestEnv().toFile();
        this.core = new Stargraph(config, false);
        core.setKBInitSet(kbId.getId());
        core.setDataRootDir(dataRootDir);
        this.core.initialize();
    }


    @Test
    public void bulkLoadTest() throws Exception {
        Indexer indexer = core.getIndexer(kbId);
        indexer.load(true, -1);
        indexer.awaitLoader();
        Searcher searcher = core.getSearcher(kbId);
        Assert.assertEquals(searcher.countDocuments(), 756);
    }
}
