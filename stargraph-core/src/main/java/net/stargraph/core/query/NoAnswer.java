package net.stargraph.core.query;

import net.stargraph.query.InteractionMode;

public final class NoAnswer extends QueryResponse {
    public NoAnswer(InteractionMode interactionMode, String userQuery) {
        super(interactionMode, userQuery);
    }
}
