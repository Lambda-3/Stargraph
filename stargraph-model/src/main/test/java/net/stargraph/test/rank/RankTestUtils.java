package net.stargraph.test.rank;

import net.stargraph.rank.Rankable;
import net.stargraph.rank.Score;

public final class RankTestUtils {

    static Rankable createRankable(String v) {
        return new Rankable() {
            @Override
            public String toString() {
                return v;
            }

            @Override
            public String getValue() {
                return v;
            }

            @Override
            public String getId() {
                return v;
            }
        };
    }

    static Score createScore(String v, double d) {
        return new Score(createRankable(v), d);
    }
}
