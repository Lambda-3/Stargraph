package net.stargraph.rank;

/*-
 * ==========================License-Start=============================
 * stargraph-model
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
import net.stargraph.rank.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public final class Rankers {
    private static Logger logger = LoggerFactory.getLogger(Rankers.class);
    private static Marker marker = MarkerFactory.getMarker("rank");

    public static Scores apply(Scores inputScores, ModifiableRankParams rankParams, String target) {
        return apply(inputScores, rankParams, asRankable(target));
    }

    public static Scores apply(Scores inputScores, ModifiableRankParams rankParams, Rankable target) {
        logger.info(marker, "Applying {}.", rankParams);
        Ranker ranker = createRanker(rankParams);
        Scores rescores = ranker.score(inputScores, target);
        return ThresholdFilter.filter(rescores, rankParams.getThreshold());
    }

    private static Ranker createRanker(ModifiableRankParams params) {
        switch (params.getRankingModel()) {
            case JACCARD:
                return new JaccardRanker();
            case JAROWINKLER:
                return new JarowinklerRanker();
            case LEVENSHTEIN:
                return new LevenshteinRanker();
            case FUZZY:
                return new FuzzyRanker();
            case W2V:
            case ESA:
                return new IndraRanker((ModifiableIndraParams) params);
        }
        throw new StarGraphException("Unknown Ranker!");
    }

    private static Rankable asRankable(String term) {
        return new Rankable() {
            @Override
            public String getValue() {
                return term;
            }

            @Override
            public String getId() {
                return term;
            }
        };
    }
}
