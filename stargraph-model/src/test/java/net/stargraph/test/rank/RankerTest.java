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

import net.stargraph.rank.Scores;
import net.stargraph.rank.impl.LevenshteinRanker;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

import static net.stargraph.test.rank.RankTestUtils.createRankable;
import static net.stargraph.test.rank.RankTestUtils.createScore;

public final class RankerTest {

    @Test
    public void levenshteinTest() {
        LevenshteinRanker ranker = new LevenshteinRanker();
        Scores scores = new Scores(Arrays.asList(createScore("lambda3", 100),
                createScore("Lambda^30", 94), createScore("Lambda^300", 51), createScore("lambda^3", 1)));

        Scores rescored = ranker.score(scores, createRankable("lambda^3"));

        Assert.assertEquals(rescored.get(0).getRankableView().getValue(), "lambda^3");
        Assert.assertEquals(rescored.get(0).getValue(), 1.0);
    }
}
