package net.stargraph.core.query.nli;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class QueryPlanPattern extends ArrayList<String> {
    private String pattern;

    public QueryPlanPattern(String pattern, List<String> triplePatterns) {
        super(triplePatterns);
        this.pattern = Objects.requireNonNull(pattern);
    }

    public boolean match(String planId) {
        return pattern.equals(Objects.requireNonNull(planId));
    }

    public String getPlanId() {
        return pattern;
    }


    @Override
    public String toString() {
        return "QueryPlanPattern{" +
                "pattern='" + pattern + '\'' +
                " triples='" + super.toString() + '\'' +
                '}';
    }
}
