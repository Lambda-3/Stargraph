package net.stargraph.core.query;

/*-
 * ==========================License-Start=============================
 * stargraph-core
 * --------------------------------------------------------------------
 * Copyright (C) 2017 Lambda^3
 * --------------------------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ==========================License-End===============================
 */

import net.stargraph.StarGraphException;
import net.stargraph.core.Namespace;
import net.stargraph.core.query.nli.DataModelBinding;
import net.stargraph.core.query.nli.QueryPlanPatterns;
import net.stargraph.rank.Score;
import net.stargraph.rank.Scores;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class SPARQLQueryBuilder {
    private QueryType queryType;
    private QueryPlanPatterns triplePatterns;
    private List<DataModelBinding> bindings;
    private Map<DataModelBinding, List<Score>> mappings;
    private Namespace namespace;

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

    public DataModelBinding getBinding(String placeHolder) {
        return bindings.stream()
                .filter(b -> b.getPlaceHolder().equals(placeHolder))
                .findFirst()
                .orElseThrow(() -> new StarGraphException("Unbounded '" + placeHolder + "'"));
    }

    List<DataModelBinding> getBindings() {
        return bindings;
    }

    void setNS(Namespace ns) {
        this.namespace = Objects.requireNonNull(ns);
    }

    boolean isResolved(DataModelBinding binding) {
        return mappings.containsKey(binding);
    }

    List<Score> getMappings(DataModelBinding binding) {
        if (mappings.containsKey(binding)) {
            return mappings.get(binding);
        }
        return Collections.emptyList();
    }

    Map<DataModelBinding, List<Score>> getMappings() {
        return mappings;
    }

    void add(DataModelBinding binding, List<Score> scores) {
        final Scores newScores = new Scores(scores.size());
        // Expanding the Namespace for all entities
        scores.forEach(s -> newScores.add(new Score(namespace.expand(s.getEntry()), s.getValue())));
        mappings.computeIfAbsent(binding, (b) -> new Scores()).addAll(newScores);
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
        List<Score> mappings = getMappings(binding);
        if (mappings.isEmpty()) {
            return Collections.singletonList(getURI(binding));
        }
        return mappings.stream()
                .map(Score::getRankableView)
                .map(r -> String.format("<%s>", unmap(r.getId()))).collect(Collectors.toList());
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

    private String unmap(String uri) {
        return namespace != null ? namespace.expandURI(uri) : uri;
    }
}
