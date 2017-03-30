package net.stargraph.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@Singleton
public final class SerializationExceptionMapper implements ExceptionMapper<JsonProcessingException> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Response toResponse(JsonProcessingException exception) {
        logger.error("Serialization Error", exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
