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

    public SPARQLQueryBuilder(QueryType queryType, QueryPlanPatterns triplePatterns, List<DataModelBinding> bindings) {
        this.queryType = Objects.requireNonNull(queryType);
        this.triplePatterns = Objects.requireNonNull(triplePatterns);
        this.bindings = Objects.requireNonNull(bindings);
        this.mappings = new ConcurrentHashMap<>();
    }

    @Override
    public String toString() {
        return build();
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
        return Collections.emptyList();
    }

    void add(DataModelBinding binding, List<Rankable> entities) {
        mappings.computeIfAbsent(binding, (b) -> new ArrayList<>()).addAll(entities);
    }

    String build() {
        switch (queryType) {
            case SELECT:
                return String.format("SELECT * WHERE {\n%s\n}", buildStatements());
            case ASK:
                return String.format("ASK {\n%s\n}", buildStatements());
            case AGGREGATE:
                throw new StarGraphException("TBD");
        }

        throw new StarGraphException("Unexpected: " + queryType);
    }

    private String buildStatements() {
        StringJoiner tripleJoiner = new StringJoiner(" . \n", "{", "}");

        triplePatterns.forEach(triplePattern -> {

            String[] components = triplePattern.getPattern().split("\\s");
            List<String> sURIs = placeHolder2URIs(components[0]);
            List<String> pURIs = placeHolder2URIs(components[1]);
            List<String> oURIs = placeHolder2URIs(components[2]);

            List<String> prod = cartesianProduct(cartesianProduct(sURIs, pURIs), oURIs);

            StringJoiner stmtJoiner = new StringJoiner("} UNION \n{");
            prod.forEach(p -> stmtJoiner.add(p.trim()));

            tripleJoiner.add(stmtJoiner.toString());
        });

        return tripleJoiner.toString();
    }

    private List<String> cartesianProduct(List<String> x, List<String> y) {
        List<String> xy = new ArrayList<>();
        x.forEach(s1 -> y.forEach(s2 -> xy.add(s1.trim() + " " + s2.trim())));
        return xy;
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
        if (mappings.isEmpty()) {
            return Collections.singletonList(getURI(binding));
        }
        return mappings.stream().map(r -> String.format("<%s>", r.getId())).collect(Collectors.toList());
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
