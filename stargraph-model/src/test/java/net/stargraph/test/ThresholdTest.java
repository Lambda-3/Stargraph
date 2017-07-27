package net.stargraph.test;

import net.stargraph.rank.Score;
import net.stargraph.rank.Scores;
import net.stargraph.rank.Threshold;
import net.stargraph.rank.ThresholdFilter;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

public final class ThresholdTest {
    Scores scores = new Scores(Arrays.asList(
            new Score("1st", 200),
            new Score("2nd", 190),
            new Score("3rd", 181),
            new Score("4th", 40),
            new Score("5th", 30),
            new Score("6th", 20),
            new Score("7th", 1)));


    @Test
    public void minTest() {
        Scores filtered = ThresholdFilter.filter(scores, Threshold.min(20));
        Assert.assertEquals(filtered, scores.subList(0, 5));
    }

    @Test
    public void maxTest() {
        Scores filtered = ThresholdFilter.filter(scores, Threshold.max(30));
        Assert.assertEquals(filtered, scores.subList(5, 7));
    }

    @Test
    public void autoTest() {
        Scores filtered = ThresholdFilter.filter(scores, Threshold.auto());
        Assert.assertEquals(filtered, scores.subList(0, 3));
    }

    @Test
    public void autoWithOneEntryTest() {
        Scores scores = new Scores(Collections.singleton(new Score("1st", 200)));
        Scores filtered = ThresholdFilter.filter(scores, Threshold.auto());
        Assert.assertEquals(filtered, scores);
    }

    @Test
    public void autoWithTwoEntriesTest() {
        Scores scores = new Scores(Arrays.asList(new Score("1st", 200), new Score("2nd", 100)));
        Scores filtered = ThresholdFilter.filter(scores, Threshold.auto());
        Assert.assertEquals(filtered, scores);
    }

    @Test
    public void autoWithConstantEntriesTest() {
        Scores scores = new Scores(Arrays.asList(new Score("1st", 100), new Score("2nd", 100), new Score("3rd", 100)));
        Scores filtered = ThresholdFilter.filter(scores, Threshold.auto());
        Assert.assertEquals(filtered, scores);
    }
}
