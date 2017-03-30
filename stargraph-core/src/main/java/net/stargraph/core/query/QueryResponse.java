package net.stargraph.core.query;

import net.stargraph.query.InteractionMode;

import java.util.Objects;

public abstract class QueryResponse {
    private String userQuery;
    private InteractionMode interactionMode;

    public QueryResponse(InteractionMode interactionMode, String userQuery) {
        this.interactionMode = Objects.requireNonNull(interactionMode);
        this.userQuery = Objects.requireNonNull(userQuery);
    }

    public InteractionMode getInteractionMode() {
        return interactionMode;
    }

    public String getUserQuery() {
        return userQuery;
    }
}
