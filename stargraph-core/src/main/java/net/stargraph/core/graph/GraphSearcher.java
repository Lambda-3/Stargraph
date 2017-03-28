package net.stargraph.core.graph;

import java.util.List;
import java.util.Map;

public interface GraphSearcher {

    Map<String, List<String>> select(String sparqlQuery);

    boolean ask(String sparqlQuery);

}
