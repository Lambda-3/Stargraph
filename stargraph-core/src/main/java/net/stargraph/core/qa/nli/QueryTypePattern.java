package net.stargraph.core.qa.nli;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class QueryTypePattern extends ArrayList<String> {
    private QueryType queryType;

    public QueryTypePattern(QueryType queryType, List<String> queryTypePatterns) {
        super(queryTypePatterns);
        this.queryType = Objects.requireNonNull(queryType);
    }

    public QueryType getQueryType() {
        return queryType;
    }
}
