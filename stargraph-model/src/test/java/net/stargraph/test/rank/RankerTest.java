package net.stargraph.test.rank;

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
