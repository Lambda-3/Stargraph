package net.stargraph.core.graph;

import net.stargraph.model.LabeledEntity;

import java.util.List;
import java.util.Map;

public interface GraphSearcher {

    Map<String, List<LabeledEntity>> select(String sparqlQuery);

    boolean ask(String sparqlQuery);

}
