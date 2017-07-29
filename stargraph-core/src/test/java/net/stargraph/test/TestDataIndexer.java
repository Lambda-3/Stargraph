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

import net.stargraph.core.IndicesFactory;
import net.stargraph.core.Stargraph;
import net.stargraph.core.index.BaseIndexer;
import net.stargraph.core.search.BaseSearcher;
import net.stargraph.model.KBId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Let's eat our own dog food for a while!
 * Indexer only stores the intended indexed expected.
 */
public final class TestDataIndexer extends BaseIndexer {

    private List<TestData> indexed;
    private long lazyTime;

    public TestDataIndexer(KBId kbId, Stargraph core, long lazyTime) {
        super(kbId, core);
        this.indexed = new ArrayList<>();
        this.lazyTime = lazyTime;
    }

    @Override
    protected void beforeLoad(boolean reset) {
        if (reset) {
            indexed.clear();
        }
    }

    @Override
    protected void doIndex(Serializable data, KBId kbId) throws InterruptedException {
        if (lazyTime > 0) {
            Thread.sleep(lazyTime);
        }

        TestData testData = (TestData) data;

        if (testData.failOnIndexer) {
            throw new TestFailureException();
        }

        System.out.println(data);
        indexed.add(testData);
    }

    final List<TestData> getIndexed() {
        return this.indexed;
    }

    static class Factory implements IndicesFactory {

        @Override
        public BaseIndexer createIndexer(KBId kbId, Stargraph core) {
            return new TestDataIndexer(kbId, core, 500);
        }

        @Override
        public BaseSearcher createSearcher(KBId kbId, Stargraph stargraph) {
            return null;
        }
    }
}
