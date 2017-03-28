package net.stargraph.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Entry point to talk with the Database.
 */
@Path("_kb")
@Produces(MediaType.APPLICATION_JSON)
public interface QueryResource {

    @GET
    @Path("{kbId}/query")
    Response query(@PathParam("kbId") String id, @QueryParam("q") String q);
}
