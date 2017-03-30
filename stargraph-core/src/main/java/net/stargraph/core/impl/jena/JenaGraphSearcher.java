package net.stargraph.core.impl.jena;

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

        try (QueryExecution qexec = QueryExecutionFactory.create(sparqlQuery, core.getModel(dbId))) {
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
