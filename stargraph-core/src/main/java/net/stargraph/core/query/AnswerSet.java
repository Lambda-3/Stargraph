package net.stargraph.core.query;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AnswerSet implements QueryResponse {
    private List<String> shortAnswer;
    private String userQuery;
    private QueryType queryType;
    private Map<String, List<String>> solutions;

    AnswerSet(String userQuery, SPARQLQueryBuilder sparqlQueryBuilder) {
        this.userQuery = Objects.requireNonNull(userQuery);
        this.queryType = Objects.requireNonNull(sparqlQueryBuilder).getQueryType();
    }

    void setShortAnswer(List<String> shortAnswer) {
        this.shortAnswer = Objects.requireNonNull(shortAnswer);
    }

    void setSolutions(Map<String, List<String>> solutions) {
        this.solutions = Objects.requireNonNull(solutions);
    }

    public List<String> getShortAnswer() {
        return shortAnswer;
    }

    public String getUserQuery() {
        return userQuery;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public Map<String, List<String>> getSolutions() {
        return solutions;
    }

    @Override
    public String toString() {
        return "AnswerSet{" +
                "shortAnswer=" + shortAnswer +
                ", userQuery='" + userQuery + '\'' +
                '}';
    }
}
