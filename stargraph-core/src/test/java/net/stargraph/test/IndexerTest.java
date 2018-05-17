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
import net.stargraph.data.Indexable;
import net.stargraph.model.KBId;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class IndexerTest {
    private static final TestData ADD_DATA = new TestData("4th");
    private static final List<TestData> EXP_AFTER_LOAD = Arrays.asList("first", "second", "third").stream().map(s -> new TestData(false, false, s)).collect(Collectors.toList());
    private static final List<TestData> EXP_AFTER_LOAD_UPDATE = Arrays.asList("first", "second", "third", "4th").stream().map(s -> new TestData(false, false, s)).collect(Collectors.toList());


    private KBId kbId = KBId.of("mytest", "mytype");
    private Stargraph stargraph;
    private Indexer indexer;

    @BeforeClass
    public void before() {
        ConfigFactory.invalidateCaches();
        Config config = ConfigFactory.load().getConfig("stargraph");
        this.stargraph = new Stargraph(config, false);
        this.stargraph.setKBInitSet(kbId.getId());
        this.stargraph.setDefaultIndicesFactory(new TestDataIndexer.Factory());
        this.stargraph.initialize();
        this.indexer = stargraph.getIndexer(kbId);
    }

    @Test(timeOut = 15000)
    public void bulkLoadTest() throws Exception {
        indexer.load(true, -1);
        indexer.await();
        Assert.assertEquals(EXP_AFTER_LOAD, ((TestDataIndexer) indexer).getIndexed());
    }

    @Test
    public void loadTwiceTest() throws Exception {
        indexer.load(true, -1);
        indexer.await();
        indexer.load();
        indexer.await();
        Assert.assertEquals(2 * EXP_AFTER_LOAD.size(), ((TestDataIndexer) indexer).getIndexed().size());
    }

    @Test
    public void loadAndUpdateTest() throws Exception {
        indexer.load(true, -1);
        indexer.await();
        indexer.update(new Indexable(ADD_DATA, kbId));
        indexer.await();
        Assert.assertEquals(EXP_AFTER_LOAD_UPDATE, ((TestDataIndexer) indexer).getIndexed());
    }

    @Test
    public void loadAndDoubleUpdateTest() throws Exception {
        indexer.load(true, -1);
        indexer.await();
        indexer.update(new Indexable(ADD_DATA, kbId));
        indexer.update(new Indexable(ADD_DATA, kbId));
        indexer.await();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void loadingWhileLoadingTest() throws Exception {
        try {
            indexer.load(true, -1);
            indexer.load();
        }
        finally {
            indexer.await();
            Assert.assertEquals(EXP_AFTER_LOAD, ((TestDataIndexer) indexer).getIndexed());
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void updatingWhileLoadingTest() throws Exception {
        try {
            indexer.load(true, -1);
            indexer.update(new Indexable(ADD_DATA, kbId));
        } finally {
            indexer.await();
            Assert.assertEquals(EXP_AFTER_LOAD, ((TestDataIndexer) indexer).getIndexed());
        }
    }

    @Test
    public void resetLoadingTest() throws Exception {
        indexer.load();
        indexer.await();
        indexer.load(true, -1);
        indexer.await();
        Assert.assertEquals(EXP_AFTER_LOAD, ((TestDataIndexer) indexer).getIndexed());
    }

    @Test
    public void limitLoadingTest() throws Exception {
        indexer.load(true, 3); //first two entries will fail for sure.
        indexer.await();
        Assert.assertEquals(((TestDataIndexer) indexer).getIndexed(),
                Collections.singletonList(new TestData(false, false, "first")));
    }

}
