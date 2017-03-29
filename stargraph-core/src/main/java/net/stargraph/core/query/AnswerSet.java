package net.stargraph.core.query;

import net.stargraph.core.query.nli.DataModelBinding;
import net.stargraph.rank.Score;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AnswerSet implements QueryResponse {
    private List<String> shortAnswer;
    private String sparqlQuery;
    private String userQuery;
    private QueryType queryType;
    private Map<DataModelBinding, List<Score>> mappings;

    AnswerSet(String userQuery, SPARQLQueryBuilder sparqlQueryBuilder) {
        this.userQuery = Objects.requireNonNull(userQuery);
        this.queryType = Objects.requireNonNull(sparqlQueryBuilder).getQueryType();
    }

    void setShortAnswer(List<String> shortAnswer) {
        this.shortAnswer = Objects.requireNonNull(shortAnswer);
    }

    void setMappings(Map<DataModelBinding, List<Score>> mappings) {
        this.mappings = Objects.requireNonNull(mappings);
    }

    void setSPARQLQuery(String sparqlQuery) {
        this.sparqlQuery = Objects.requireNonNull(sparqlQuery);
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

    public Map<DataModelBinding, List<Score>> getMappings() {
        return mappings;
    }

    @Override
    public String toString() {
        return "AnswerSet{" +
                "shortAnswer=" + shortAnswer +
                ", userQuery='" + userQuery + '\'' +
                '}';
    }
}
