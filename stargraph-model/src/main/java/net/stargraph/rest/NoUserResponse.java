package net.stargraph.rest;

import net.stargraph.query.InteractionMode;

public final class NoUserResponse extends UserResponse {
    public final String msg = "No Answer.";

    public NoUserResponse(String query, InteractionMode interactionMode) {
        super(query, interactionMode);
    }
}
