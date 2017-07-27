package net.stargraph.test.rank;

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

import net.stargraph.rank.ModifiableIndraParams;
import net.stargraph.rank.ParamsBuilder;
import net.stargraph.rank.Ranker;
import net.stargraph.rank.Scores;
import net.stargraph.rank.impl.IndraRanker;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

import static net.stargraph.test.rank.RankTestUtils.createRankable;
import static net.stargraph.test.rank.RankTestUtils.createScore;

public final class RankerIT {

    @Test
    public void indraRankerTest() {
        ModifiableIndraParams params = ParamsBuilder.word2vec().corpus("wiki-2014").language("EN").url(getIndraURL());
        Ranker ranker = new IndraRanker(params);

        Scores scores = new Scores(Arrays.asList(createScore("husband", 100), createScore("husband", 100),
                createScore("children", 94), createScore("partner", 51), createScore("father", 1)));

        Scores rescored = ranker.score(scores, createRankable("wife"));
        Assert.assertEquals(rescored.get(0).getRankableView().getValue(), "husband");
    }

    private static String getIndraURL() {
        return System.getProperty("stargraph.distributional-service.rest-url", "http://indra.lambda3.org/relatedness");
    }
}
