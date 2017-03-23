package net.stargraph.core.query;

import net.stargraph.StarGraphException;
import net.stargraph.core.query.nli.DataModelBinding;
import net.stargraph.core.query.nli.QueryPlanPattern;
import net.stargraph.core.query.nli.QueryType;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public final class SPARQLQuery {
    private QueryType queryType;
    private QueryPlanPattern triplePatterns;
    private List<DataModelBinding> bindings;
    private String sparqlQueryStr;

    public SPARQLQuery(QueryType queryType, QueryPlanPattern triplePatterns, List<DataModelBinding> bindings) {
        this.queryType = Objects.requireNonNull(queryType);
        this.triplePatterns = Objects.requireNonNull(triplePatterns);
        this.bindings = Objects.requireNonNull(bindings);
        this.sparqlQueryStr = createQueryString();
    }

    @Override
    public String toString() {
        return sparqlQueryStr;
    }

    public QueryPlanPattern getTriplePatterns() {
        return triplePatterns;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public DataModelBinding getBinding(String placeHolder) {
        return bindings.stream()
                .filter(b -> b.getPlaceHolder().equals(placeHolder))
                .findFirst()
                .orElseThrow(() -> new StarGraphException("Unbounded '" + placeHolder + "'"));
    }

    private String createQueryString() {
        switch (queryType) {
            case SELECT:
                return String.format("SELECT * WHERE {\n %s \n}", buildStatements());
            case ASK:
                return String.format("ASK {\n %s \n}", buildStatements());
            case AGGREGATE:
                throw new StarGraphException("TBD");
        }

        throw new StarGraphException("Unexpected: " + queryType);
    }

    private String buildStatements() {
        StringJoiner tripleJoiner = new StringJoiner(" . \n", "{ ", " }");

        triplePatterns.forEach(triplePattern -> {
            StringJoiner stmtJoiner = new StringJoiner(" ");
            for (String placeHolder : triplePattern.split("\\s")) {
                if (!isVar(placeHolder)) {
                    DataModelBinding binding = getBinding(placeHolder);
                    stmtJoiner.add(getURI(binding));
                }
                else {
                    stmtJoiner.add(placeHolder);
                }
            }
            tripleJoiner.add(stmtJoiner.toString());
        });

        return tripleJoiner.toString();
    }

    private boolean isVar(String s) {
        return s.startsWith("?VAR");
    }

    private String getURI(DataModelBinding binding) {
        return String.format(":%s", binding.getTerm().replaceAll("\\s", "_"));
    }
}
