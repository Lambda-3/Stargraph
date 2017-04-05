package net.stargraph.core.query.response;

import net.stargraph.core.query.QueryResponse;
import net.stargraph.model.LabeledEntity;
import net.stargraph.query.InteractionMode;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SPARQLSelectResponse extends QueryResponse {
    private Map<String, List<LabeledEntity>> bindings;

    public SPARQLSelectResponse(InteractionMode interactionMode, String userQuery, Map<String, List<LabeledEntity>> bindings) {
        super(interactionMode, userQuery);
        this.bindings = Objects.requireNonNull(bindings);
    }

    public Map<String, List<LabeledEntity>> getBindings() {
        return bindings;
    }

    @Override
    public String toString() {
        return "SPARQLResponse{" +
                "query='" + getUserQuery() + '\'' +
                ", bindings=" + bindings +
                '}';
    }
}
