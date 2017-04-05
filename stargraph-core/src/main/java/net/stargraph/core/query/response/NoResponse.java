package net.stargraph.core.query.response;

import net.stargraph.core.query.QueryResponse;
import net.stargraph.query.InteractionMode;

public final class NoResponse extends QueryResponse {
    public NoResponse(InteractionMode interactionMode, String userQuery) {
        super(interactionMode, userQuery);
    }
}
