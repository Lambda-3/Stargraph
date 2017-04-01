package net.stargraph.core.impl.jena;

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

import net.stargraph.core.Stargraph;
import net.stargraph.core.graph.GraphSearcher;
import net.stargraph.core.search.EntitySearcher;
import net.stargraph.model.LabeledEntity;
import net.stargraph.model.ValueEntity;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.*;

public final class JenaGraphSearcher implements GraphSearcher {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Marker marker = MarkerFactory.getMarker("jena");
    private Stargraph core;
    private String dbId;

    public JenaGraphSearcher(String dbId, Stargraph core) {
        this.core = Objects.requireNonNull(core);
        this.dbId = Objects.requireNonNull(dbId);
    }

    @Override
    public Map<String, List<LabeledEntity>> select(String sparqlQuery) {
        return doSparqlQuery(sparqlQuery);
    }

    @Override
    public boolean ask(String sparqlQuery) {
        return false;
    }

    private Map<String, List<LabeledEntity>> doSparqlQuery(String sparqlQuery) {
        logger.info(marker, "Executing: {}", sparqlQuery);

        long startTime = System.currentTimeMillis();

        Map<String, List<LabeledEntity>> result = new LinkedHashMap<>();
        EntitySearcher entitySearcher = core.createEntitySearcher();

        try (QueryExecution qexec = QueryExecutionFactory.create(sparqlQuery, core.getGraphModel(dbId))) {
            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {
                Binding jBinding = results.nextBinding();
                Iterator<Var> vars = jBinding.vars();
                while (vars.hasNext()) {
                    Var jVar = vars.next();

                    if (!jBinding.get(jVar).isLiteral()) {
                        String id = jBinding.get(jVar).getURI();
                        result.computeIfAbsent(jVar.getVarName(),
                                (v) -> new ArrayList<>()).add(entitySearcher.getEntity(dbId, id));
                    } else {
                        LiteralLabel lit = jBinding.get(jVar).getLiteral();
                        ValueEntity valueEntity = new ValueEntity(lit.getLexicalForm(), lit.getDatatype().getURI(), lit.language());
                        result.computeIfAbsent(jVar.getVarName(), (v) -> new ArrayList<>()).add(valueEntity);
                    }
                }
            }
        }

        long millis = System.currentTimeMillis() - startTime;
        logger.info(marker, "SPARQL query took {}s", millis / 1000.0);
        return result;
    }
}
