package net.stargraph.server;

/*-
 * ==========================License-Start=============================
 * stargraph-server
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

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import net.stargraph.StarGraphException;
import net.stargraph.core.Stargraph;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.net.URI;
import java.nio.file.Paths;

public final class Server {
    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private static Logger logger = LoggerFactory.getLogger(Server.class);
    private static Marker marker = MarkerFactory.getMarker("server");
    private HttpServer httpServer;
    private Stargraph core;

    Server(Stargraph core) {
        Preconditions.checkNotNull(core);
        this.core = core;
    }

    void start() {
        try {
            Config config = core.getConfig();
            String urlStr = config.getString("networking.rest-url");
            ResourceConfig rc = new ResourceConfig();
            rc.register(CORSFilters.class);
            rc.register(LoggingFilter.class);
            rc.register(JacksonFeature.class);
            rc.register(CatchAllExceptionMapper.class);
            rc.register(SerializationExceptionMapper.class);
            rc.register(WebAppExceptionMapper.class);
            rc.register(AdminResourceImpl.class);
            rc.register(new KBResourceImpl(core));
            rc.register(new QueryResourceImpl(core));
            httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(urlStr), rc, true);
            logger.info(marker, "Stargraph listening on {}", urlStr);
        } catch (Exception e) {
            throw new StarGraphException(e);
        }
    }

    void stop() {
        try {
            if (httpServer != null && httpServer.isStarted()) {
                httpServer.shutdownNow();
            }
        } catch (Exception e) {
            logger.error(marker, "Error while terminating HTTP httpServer", e);
            e.printStackTrace(System.err);
        }
    }

    /**
     * Application launcher. Exposing REST API.
     */
    public static void main(String args[]) {
        if (args.length == 0) {
            throw new StarGraphException("Missing home directory as argument!");
        }

        final Stargraph core = new Stargraph(Paths.get(args[0]).toFile());
        final Server server = new Server(core);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info(marker, "Going down...");
            server.stop();
            core.terminate();
        }));
    }

}
