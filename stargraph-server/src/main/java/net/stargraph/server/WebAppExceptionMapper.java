package net.stargraph.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@Singleton
public class WebAppExceptionMapper implements ExceptionMapper<WebApplicationException> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Response toResponse(WebApplicationException exception) {
        logger.error("Web Application Error!", exception);
        return exception.getResponse();
    }
}
