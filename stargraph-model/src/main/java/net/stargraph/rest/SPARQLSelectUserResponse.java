package net.stargraph.rest;

import net.stargraph.query.InteractionMode;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SPARQLSelectUserResponse extends UserResponse {
    private Map<String, List<String>> bindings;

    public SPARQLSelectUserResponse(String query, InteractionMode interactionMode) {
        super(query, interactionMode);
    }

    public void setBindings(Map<String, List<String>> bindings) {
        this.bindings = Objects.requireNonNull(bindings);
    }

    public Map<String, List<String>> getBindings() {
        return bindings;
    }
}
