package net.stargraph.core.query.nli;

import net.stargraph.core.query.TriplePattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class QueryPlanPatterns extends ArrayList<TriplePattern> {
    private String pattern;

    public QueryPlanPatterns(String pattern, List<TriplePattern> triplePatterns) {
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
