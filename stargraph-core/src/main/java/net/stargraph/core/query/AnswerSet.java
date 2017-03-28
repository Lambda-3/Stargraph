package net.stargraph.core.query;

import java.util.Objects;

public final class AnswerSet {
    private String shortAnswer;
    private String userQuery;
    private QueryType queryType;

    AnswerSet(String userQuery, SPARQLQueryBuilder sparqlQueryBuilder) {
        this.userQuery = Objects.requireNonNull(userQuery);
        this.queryType = Objects.requireNonNull(sparqlQueryBuilder).getQueryType();
    }

}
