package net.stargraph.core.query.agnostic;

import net.stargraph.core.query.nli.DataModelBinding;
import net.stargraph.core.query.nli.QueryPlanPattern;
import net.stargraph.core.query.nli.QueryType;

import java.util.List;
import java.util.Objects;

public final class SchemaAgnosticSPARQL {
    private QueryType queryType;
    private QueryPlanPattern triplePatterns;
    private List<DataModelBinding> bindings;


    public SchemaAgnosticSPARQL(QueryType queryType, QueryPlanPattern triplePatterns, List<DataModelBinding> bindings) {
        this.queryType = Objects.requireNonNull(queryType);
        this.triplePatterns = Objects.requireNonNull(triplePatterns);
        this.bindings = Objects.requireNonNull(bindings);
    }

    @Override
    public String toString() {
        return "SchemaAgnosticSPARQL{" +
                "queryType=" + queryType +
                ", triplePatterns=" + triplePatterns +
                ", bindings=" + bindings +
                '}';
    }
}
