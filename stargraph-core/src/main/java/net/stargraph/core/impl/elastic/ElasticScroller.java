package net.stargraph.core.impl.elastic;

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

import net.stargraph.core.search.SearchQueryHolder;
import net.stargraph.rank.Score;
import net.stargraph.rank.Scores;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper around the ES Scrolling API.
 */
public abstract class ElasticScroller implements Iterable<Score> {
    private static String scrollTimeKey = "stargraph.elastic.scroll.time";
    private static String scrollSizeKey = "stargraph.elastic.scroll.size";

    private Logger logger = LoggerFactory.getLogger(getClass());
    private Marker marker = MarkerFactory.getMarker("elastic");

    private ElasticClient elasticClient;
    private SearchQueryHolder<QueryBuilder> holder;
    private int maxEntries;
    private int consumedEntries;
    private InnerIterator innerIterator;
    private TimeValue scrollTime;
    private int maxScrollSize;
    private String[] fields;

    public ElasticScroller(ElasticClient client, SearchQueryHolder<QueryBuilder> holder) {
        this.elasticClient = Objects.requireNonNull(client);
        this.holder = Objects.requireNonNull(holder);
        this.fields = new String[]{"_source"};
        this.maxEntries = holder.getSearchParams().getLimit();
        this.scrollTime = new TimeValue(Integer.valueOf(System.getProperty(scrollTimeKey, "120")), TimeUnit.SECONDS);
        this.maxScrollSize = Integer.valueOf(System.getProperty(scrollSizeKey, "8000"));
        logger.trace(marker, "{}={}", scrollTimeKey, scrollTime);
        logger.trace(marker, "{}={}", scrollSizeKey, maxScrollSize);
    }

    @Override
    public Iterator<Score> iterator() {
        logger.trace(marker, "Creating new scroller for {} with query: {}", elasticClient, holder.getQuery());
        consumedEntries = 0;
        innerIterator = new InnerIterator();
        return innerIterator;
    }

    public Scores getScores() {
        Scores scores = new Scores();
        this.forEach(scores::add);
        return scores;
    }

    protected abstract Score build(SearchHit hit);

    private class InnerIterator implements Iterator<Score> {
        SearchResponse response;
        Iterator<SearchHit> innerIt;
        String scrollId;

        @Override
        public boolean hasNext() {
            boolean hasNext = false;

            try {
                if (innerIt == null) {
                    response = elasticClient.prepareSearch()
                            .setScroll(scrollTime)
                            .setQuery(holder.getQuery())
                            .storedFields(fields)
                            .setSize(maxScrollSize).get();

                    ESUtils.check(response);

                    innerIt = response.getHits().iterator();
                    hasNext = innerIt.hasNext();

                    if (hasNext) {
                        scrollId = response.getScrollId();
                        logger.trace(marker, "Iterating over {}", response.getHits().totalHits());
                    }
                } else {
                    hasNext = innerIt.hasNext();

                    if (!hasNext) {
                        logger.trace(marker, "Preparing new batch..");
                        response = elasticClient.prepareSearchScroll(scrollId).setScroll(scrollTime).get();
                        scrollId = response.getScrollId();
                        innerIt = response.getHits().iterator();
                        hasNext = innerIt.hasNext();
                        logger.trace(marker, "scrollId: {}", scrollId);
                    }
                }

                hasNext = hasNext && (maxEntries < 0 || consumedEntries < maxEntries);
                return hasNext;
            } finally {
                if (!hasNext && scrollId != null) {
                    if (response.getScrollId() != null) {
                        logger.trace(marker, "Clearing scrolling context.");
                        ClearScrollResponse res = elasticClient.prepareClearScroll(scrollId).get();
                        if (!res.isSucceeded()) {
                            logger.warn(marker, "Fail to clear scroll {}", scrollId);
                        }
                    }
                }
            }
        }

        @Override
        public Score next() {
            try {
                Score score = build(innerIt.next());
                if (score == null) {
                    throw new IllegalStateException("Can't return a NULL entry");
                }
                consumedEntries++;
                return score;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
