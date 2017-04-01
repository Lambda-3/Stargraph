package net.stargraph.server;

import net.stargraph.StarGraphException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@Singleton
public final class CatchAllExceptionMapper implements ExceptionMapper<Exception> {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Marker marker = MarkerFactory.getMarker("server");

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof StarGraphException) {
            return ResourceUtils.createAckResponse((StarGraphException) exception);
        }

        if (exception instanceof WebApplicationException) {
            return ((WebApplicationException) exception).getResponse();
        }

        logger.error(marker, "Oops!", exception);
        return Response.status(500).build();
    }
}
