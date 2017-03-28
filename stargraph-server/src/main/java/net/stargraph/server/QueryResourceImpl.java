package net.stargraph.server;

import net.stargraph.core.Stargraph;
import net.stargraph.core.query.QueryEngine;
import net.stargraph.rest.QueryResource;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class QueryResourceImpl implements QueryResource {

    private Stargraph core;
    private Map<String, QueryEngine> engines;

    public QueryResourceImpl(Stargraph core) {
        this.core = Objects.requireNonNull(core);
        this.engines = new ConcurrentHashMap<>();
    }

    @Override
    public Response query(String id, String q) {
        if (core.hasKB(id)) {
            QueryEngine engine = engines.computeIfAbsent(id, (k) -> new QueryEngine(k, core));
            return Response.status(Response.Status.OK).entity(engine.query(q)).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
