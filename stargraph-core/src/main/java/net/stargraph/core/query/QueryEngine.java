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

import net.stargraph.query.Language;
import net.stargraph.StarGraphException;
import net.stargraph.core.Namespace;
import net.stargraph.core.Stargraph;
import net.stargraph.core.graph.GraphSearcher;
import net.stargraph.core.query.nli.*;
import net.stargraph.core.search.EntitySearcher;
import net.stargraph.model.InstanceEntity;
import net.stargraph.model.LabeledEntity;
import net.stargraph.query.InteractionMode;
import net.stargraph.rank.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.stargraph.query.InteractionMode.NLI;

public final class QueryEngine {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Marker marker = MarkerFactory.getMarker("query");

    private String dbId;
    private Stargraph core;
    private Analyzers analyzers;
    private GraphSearcher graphSearcher;
    private Namespace namespace;
    private Language language;

    public QueryEngine(String dbId, Stargraph core) {
        this.dbId = Objects.requireNonNull(dbId);
        this.core = Objects.requireNonNull(core);
        this.analyzers = new Analyzers(core.getConfig());
        this.graphSearcher = core.createGraphSearcher(dbId);
        this.namespace = core.getNamespace(dbId);
        this.language = core.getLanguage(dbId);
    }

    public QueryResponse query(String query) {
        long startTime = System.currentTimeMillis();
        try {
            switch (detectInteractionMode(query)) {
                case NLI:
                    return nliQuery(query, language);
            }
            throw new StarGraphException("Input type not yet supported");

        }
        catch (Exception e) {
            logger.error(marker, "Query Error '{}'", query, e);
            throw new StarGraphException("Query Error", e);
        }
        finally {
            long millis = System.currentTimeMillis() - startTime;
            logger.info(marker, "Query Engine took {}s",  millis / 1000.0);
        }
    }

    private QueryResponse nliQuery(String userQuery, Language language) {
        QuestionAnalyzer analyzer = this.analyzers.getQuestionAnalyzer(language);
        QuestionAnalysis analysis = analyzer.analyse(userQuery);
        SPARQLQueryBuilder queryBuilder = analysis.getSPARQLQueryBuilder();
        queryBuilder.setNS(namespace);

        QueryPlanPatterns triplePatterns = queryBuilder.getTriplePatterns();
        List<DataModelBinding> bindings = queryBuilder.getBindings();

        triplePatterns.forEach(triplePattern -> {
            logger.debug(marker, "Resolving {}", triplePattern);
            resolve(asTriple(triplePattern, bindings), queryBuilder);
        });

        String sparqlQueryStr = queryBuilder.build();

        Map<String, List<LabeledEntity>> vars = graphSearcher.select(sparqlQueryStr);

        if (!vars.isEmpty()) {
            AnswerSet answerSet = new AnswerSet(NLI, userQuery, queryBuilder);
            answerSet.setShortAnswer(vars.get("VAR_1")); // convention, answer must be bound to the first var
            answerSet.setMappings(queryBuilder.getMappings());
            answerSet.setSPARQLQuery(sparqlQueryStr);
            return answerSet;
        }

        logger.warn(marker, "No Answer for '{}'", userQuery);
        return new NoAnswer(NLI, userQuery);
    }

    private InteractionMode detectInteractionMode(String queryString) {
        String q = queryString.trim();
        if (q.endsWith("?")) {
            return NLI;
        }
        throw new StarGraphException("Input type not yet supported");
    }


    private void resolve(Triple triple, SPARQLQueryBuilder builder) {
        InstanceEntity pivot = resolvePivot(triple.s, builder);
        pivot = pivot != null ? pivot : resolvePivot(triple.o, builder);
        resolvePredicate(pivot, triple.p, builder);
    }

    private void resolvePredicate(InstanceEntity pivot, DataModelBinding binding, SPARQLQueryBuilder builder) {
        if ((binding.getModelType() == DataModelType.CLASS
                || binding.getModelType() == DataModelType.PROPERTY) && !builder.isResolved(binding)) {

            EntitySearcher searcher = core.createEntitySearcher();
            ModifiableSearchParams searchParams = ModifiableSearchParams.create(dbId).term(binding.getTerm());
            ModifiableRankParams rankParams = ParamsBuilder.word2vec();
            Scores scores = searcher.pivotedSearch(pivot, searchParams, rankParams);
            builder.add(binding, scores.stream().limit(3).collect(Collectors.toList()));
        }
    }

    private InstanceEntity resolvePivot(DataModelBinding binding, SPARQLQueryBuilder builder) {
        List<Score> mappings = builder.getMappings(binding);
        if (!mappings.isEmpty()) {
            return (InstanceEntity)mappings.get(0).getEntry();
        }

        if (binding.getModelType() == DataModelType.INSTANCE) {
            EntitySearcher searcher = core.createEntitySearcher();
            ModifiableSearchParams searchParams = ModifiableSearchParams.create(dbId).term(binding.getTerm());
            ModifiableRankParams rankParams = ParamsBuilder.levenshtein(); // threshold defaults to auto
            Scores scores = searcher.instanceSearch(searchParams, rankParams);
            InstanceEntity instance = (InstanceEntity) scores.get(0).getEntry();
            builder.add(binding, Collections.singletonList(scores.get(0)));
            return instance;
        }
        return null;
    }

    private Triple asTriple(TriplePattern pattern, List<DataModelBinding> bindings) {
        String[] components = pattern.getPattern().split("\\s");
        return new Triple(map(components[0], bindings), map(components[1], bindings), map(components[2], bindings));
    }

    private DataModelBinding map(String placeHolder, List<DataModelBinding> bindings) {
        if (placeHolder.startsWith("?VAR") || placeHolder.startsWith("TYPE")) {
            DataModelType type = placeHolder.startsWith("?VAR") ? DataModelType.VARIABLE : DataModelType.TYPE;
            return new DataModelBinding(type, placeHolder, placeHolder);
        }

        return bindings.stream()
                .filter(b -> b.getPlaceHolder().equals(placeHolder))
                .findAny().orElseThrow(() -> new StarGraphException("Unmapped placeholder '" + placeHolder + "'"));
    }

    private static class Triple {
        Triple(DataModelBinding s, DataModelBinding p, DataModelBinding o) {
            this.s = s;
            this.p = p;
            this.o = o;
        }

        public DataModelBinding s;
        public DataModelBinding p;
        public DataModelBinding o;

        @Override
        public String toString() {
            return "Triple{" +
                    "s=" + s +
                    ", p=" + p +
                    ", o=" + o +
                    '}';
        }
    }
}
