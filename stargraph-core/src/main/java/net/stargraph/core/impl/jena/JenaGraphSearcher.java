package net.stargraph.core.impl.jena;

import net.stargraph.core.graph.GraphSearcher;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
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
    private Model model;

    public JenaGraphSearcher(Model model) {
        this.model = Objects.requireNonNull(model);
    }

    @Override
    public Map<String, List<String>> select(String sparqlQuery) {
        return doSparqlQuery(sparqlQuery);
    }

    @Override
    public boolean ask(String sparqlQuery) {
        return false;
    }

    private Map<String, List<String>> doSparqlQuery(String sparqlQuery) {
        logger.info(marker, "Executing: {}", sparqlQuery);

        long startTime = System.currentTimeMillis();

        Map<String, List<String>> bindings = new LinkedHashMap<>();

        try (QueryExecution qexec = QueryExecutionFactory.create(sparqlQuery, model)) {
            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {
                Binding jBinding = results.nextBinding();
                Iterator<Var> vars = jBinding.vars();
                while (vars.hasNext()) {
                    Var jVar = vars.next();
                    List<String> entities = bindings.getOrDefault(jVar.getVarName(), new ArrayList<>());

                    String id;

                    if (!jBinding.get(jVar).isLiteral()) {
                        id = jBinding.get(jVar).getURI();
                    } else {
                        LiteralLabel lit = jBinding.get(jVar).getLiteral();
                        id = lit.getLexicalForm();
                    }

                    entities.add(id);
                    bindings.put(jVar.getVarName(), entities);
                }
            }
        }

        long millis = System.currentTimeMillis() - startTime;
        logger.info(marker, "SPARQL query took {}s", millis / 1000.0);
        return bindings;
    }
}
