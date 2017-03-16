package net.stargraph.core.qa.nli;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class QueryPlan extends ArrayList<String> {
    private String pattern;

    public QueryPlan(String pattern, List<String> triplePatterns) {
        super(triplePatterns);
        this.pattern = Objects.requireNonNull(pattern);
    }

    public String getPattern() {
        return pattern;
    }
}
