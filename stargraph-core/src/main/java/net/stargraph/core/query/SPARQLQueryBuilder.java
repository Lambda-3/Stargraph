package net.stargraph.core.query;

import net.stargraph.StarGraphException;
import net.stargraph.core.query.nli.DataModelBinding;
import net.stargraph.core.query.nli.QueryPlanPatterns;
import net.stargraph.rank.Rankable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class SPARQLQueryBuilder {
    private QueryType queryType;
    private QueryPlanPatterns triplePatterns;
    private List<DataModelBinding> bindings;
    private Map<DataModelBinding, List<Rankable>> mappings;
    private String sparqlQueryStr;

    public SPARQLQueryBuilder(QueryType queryType, QueryPlanPatterns triplePatterns, List<DataModelBinding> bindings) {
        this.queryType = Objects.requireNonNull(queryType);
        this.triplePatterns = Objects.requireNonNull(triplePatterns);
        this.bindings = Objects.requireNonNull(bindings);
        this.mappings = new ConcurrentHashMap<>();
        this.sparqlQueryStr = build();
    }

    @Override
    public String toString() {
        return sparqlQueryStr;
    }

    public QueryPlanPatterns getTriplePatterns() {
        return triplePatterns;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public List<DataModelBinding> getBindings() {
        return bindings;
    }

    public DataModelBinding getBinding(String placeHolder) {
        return bindings.stream()
                .filter(b -> b.getPlaceHolder().equals(placeHolder))
                .findFirst()
                .orElseThrow(() -> new StarGraphException("Unbounded '" + placeHolder + "'"));
    }

    boolean isResolved(DataModelBinding binding) {
        return mappings.containsKey(binding);
    }

    List<Rankable> getSolutions(DataModelBinding binding) {
        if (mappings.containsKey(binding)) {
            return mappings.get(binding);
        }
        return null;
    }

    void add(DataModelBinding binding, Rankable entity) {
        mappings.computeIfAbsent(binding, (b) -> new ArrayList<>()).add(entity);
    }

    String build() {
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
            for (String placeHolder : triplePattern.getPattern().split("\\s")) {
                if (!isVar(placeHolder)) {
                    if (!placeHolder.equals("TYPE")) {
                        DataModelBinding binding = getBinding(placeHolder);
                        stmtJoiner.add(getURI(binding));
                    }
                    else {
                        stmtJoiner.add("a");
                    }
                }
                else {
                    stmtJoiner.add(placeHolder);
                }
            }
            tripleJoiner.add(stmtJoiner.toString());
        });

        return tripleJoiner.toString();
    }

    private List<String> placeHolder2URIs(String placeHolder) {
        if (isVar(placeHolder)) {
            return Collections.singletonList(placeHolder);
        }

        if (isType(placeHolder)) {
            return Collections.singletonList("a");
        }

        DataModelBinding binding = getBinding(placeHolder);
        List<Rankable> mappings = getSolutions(binding);
        return mappings.stream().map(r -> r.getValue()).collect(Collectors.toList());
    }

    private boolean isVar(String s) {
        return s.startsWith("?VAR");
    }

    private boolean isType(String s) {
        return s.startsWith("TYPE");
    }

    private String getURI(DataModelBinding binding) {
        return String.format(":%s", binding.getTerm().replaceAll("\\s", "_"));
    }
}
