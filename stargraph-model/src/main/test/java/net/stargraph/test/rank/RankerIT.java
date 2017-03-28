package net.stargraph.test.rank;

import net.stargraph.rank.ModifiableIndraParams;
import net.stargraph.rank.ParamsBuilder;
import net.stargraph.rank.Ranker;
import net.stargraph.rank.Scores;
import net.stargraph.rank.impl.IndraRanker;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

import static net.stargraph.test.rank.RankTestUtils.create;

public final class RankerIT {

    @Test
    public void indraRankerTest() {
        ModifiableIndraParams params = ParamsBuilder.word2vec().corpus("wiki-2014").language("EN").url(getIndraURL());
        Ranker ranker = new IndraRanker(params);

        Scores scores = new Scores(Arrays.asList(create("husband", 100), create("husband", 100),
                create("children", 94), create("partner", 51), create("father", 1)));

        Scores rescored = ranker.score(scores, () -> "wife");
        Assert.assertEquals(rescored.get(0).getRankableView().getValue(), "husband");
    }

    private static String getIndraURL() {
        return System.getProperty("stargraph.distributional-service.rest-url", "http://indra.lambda3.org/relatedness");
    }
}
