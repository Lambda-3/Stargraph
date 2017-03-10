package net.stargraph.test.rank;

import net.stargraph.rank.Rankable;
import net.stargraph.rank.Score;

public final class RankTestUtils {

    static Score create(String v, double d) {
        return new Score(new Rankable() {

            @Override
            public String toString() {
                return v;
            }

            @Override
            public String getValue() {
                return v;
            }

        }, d);
    }
}
