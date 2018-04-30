package net.stargraph.core.impl.lucene;

import net.stargraph.StarGraphException;
import net.stargraph.core.search.SearchQueryHolder;
import net.stargraph.rank.Score;
import net.stargraph.rank.Scores;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public abstract class LuceneScroller implements Iterable<Score> {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Marker marker = MarkerFactory.getMarker("lucene");

    private IndexSearcher indexSearcher;
    private SearchQueryHolder<Query> holder;
    private int maxEntries;
    private InnerIterator innerIterator;

    public LuceneScroller(IndexSearcher indexSearcher, SearchQueryHolder<Query> holder) {
        this.indexSearcher = Objects.requireNonNull(indexSearcher);
        this.holder = Objects.requireNonNull(holder);
        this.maxEntries = holder.getSearchParams().getLimit();

    }

    @Override
    public Iterator<Score> iterator() {
        logger.trace(marker, "Creating new scroller for {} with query: {}", indexSearcher, holder.getQuery());
        innerIterator = new InnerIterator();
        return innerIterator;
    }

    public Scores getScores() {
        Scores scores = new Scores();
        this.forEach(scores::add);
        return scores;
    }

    protected abstract Score build(ScoreDoc hit);

    private class InnerIterator implements Iterator<Score> {
        Iterator<ScoreDoc> innerIt;

        @Override
        public boolean hasNext() {
            boolean hasNext = false;

            try {
                if (innerIt == null) {
                    // TODO instead of setting Integer.MAX_VALUE, use low-level search(Query query, HitCollector results) instead?
                    TopDocs response = indexSearcher.search(
                            holder.getQuery(),
                            (maxEntries < 0) ? Integer.MAX_VALUE : maxEntries,
                            Sort.RELEVANCE);

                    innerIt = Arrays.asList(response.scoreDocs).iterator();
                    hasNext = innerIt.hasNext();

                    if (hasNext) {
                        logger.trace(marker, "Iterating over {}", response.totalHits);
                    }

                } else {
                    hasNext = innerIt.hasNext();
                }

                return hasNext;
            } catch (IOException e) {
                throw new StarGraphException(e);
            } finally {

            }
        }

        @Override
        public Score next() {
            try {
                Score score = build(innerIt.next());
                if (score == null) {
                    throw new IllegalStateException("Can't return a NULL entry");
                }
                return score;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
