package net.stargraph.rest;

/*-
 * ==========================License-Start=============================
 * stargraph-model
 * --------------------------------------------------------------------
 * Copyright (C) 2017 Lambda^3
 * --------------------------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ==========================License-End===============================
 */

import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Related to operations on the configured Knowledge Bases.
 */
@Path("_kb")
@Produces(MediaType.APPLICATION_JSON)
public interface KBResource {

    @GET
    List<String> getKBs();

    @POST
    @Path("{id}/_load/{type}")
    Response load(@PathParam("id") String id, @PathParam("type") String type,
                  @DefaultValue("true") @QueryParam("reset") boolean reset,
                  @DefaultValue("-1") @QueryParam("limit") int limit);

    @POST
    @Path("{id}/_load")
    Response loadAll(@PathParam("id") String id, @QueryParam("resetKey") String resetKey);

    @POST
    @Path("{id}/_upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response upload(@PathParam("id") String id, FormDataMultiPart form);

    @POST
    @Path("{id}/_clear/{type}")
    Response clear(@PathParam("id") String id, @PathParam("type") String type);
}
