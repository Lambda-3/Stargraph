package net.stargraph.core.query.nli;

import net.stargraph.core.query.QueryType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public final class QueryTypePatterns extends ArrayList<Pattern> {
    private QueryType queryType;

    public QueryTypePatterns(QueryType queryType, List<Pattern> queryTypePatterns) {
        super(Objects.requireNonNull(queryTypePatterns));
        this.queryType = Objects.requireNonNull(queryType);
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public boolean match(String query) {
        return this.parallelStream().anyMatch(p -> p.matcher(query).matches());
    }
}
