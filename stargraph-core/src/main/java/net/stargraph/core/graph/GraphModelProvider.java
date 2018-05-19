package net.stargraph.core.graph;

/*-
 * ==========================License-Start=============================
 * stargraph-core
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

import net.stargraph.StarGraphException;
import net.stargraph.core.Stargraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Primary graph model generating interface.
 */
public class GraphModelProvider {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected Marker marker = MarkerFactory.getMarker("graph");

    private Stargraph stargraph;
    private String dbId;
    private boolean inMemory;
    private boolean reset;
    private List<GraphSource<BaseGraphModel>> graphSources;

    public GraphModelProvider(Stargraph stargraph, String dbId, boolean inMemory, boolean reset, GraphSource<BaseGraphModel> graphSource) {
        this(stargraph, dbId, inMemory, reset, Arrays.asList(graphSource));
    }

    public GraphModelProvider(Stargraph stargraph, String dbId, boolean inMemory, boolean reset, List<GraphSource<BaseGraphModel>> graphSources) {
        this.stargraph = stargraph;
        this.dbId = dbId;
        this.inMemory = inMemory;
        this.reset = reset;
        this.graphSources = Objects.requireNonNull(graphSources);
    }

    public BaseGraphModel createGraphModel() {
        BaseGraphModel graphModel;
        boolean create = true;

        if (inMemory) {
            logger.info(marker, "Prepare an in-memory graph model.");
            graphModel = new MGraphModel();
        } else {
            logger.info(marker, "Prepare a stored graph model [reset={}].", reset);
            Path storePath = Paths.get(stargraph.getGraphModelDataDir(dbId), "tdb-store");

            if (storePath.toFile().exists() && !reset) {
                create = false;
            }
            graphModel = new SGraphModel(storePath.toString());
        }

        if (reset) {
            graphModel.reset();
        }

        if (create) {

            // Create graph model
            try {
                logger.info(marker, "Create graph model. This can take some time ;) ..");
                long startTime = System.nanoTime() / 1000_000;

                graphSources.forEach(s -> s.extend(graphModel));

                long elapsedTime = (System.nanoTime() / 1000_000) - startTime;
                logger.info(marker, "Graph model created in {} min, {} sec.",
                        TimeUnit.MILLISECONDS.toMinutes(elapsedTime),
                        TimeUnit.MILLISECONDS.toSeconds(elapsedTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTime))
                );
            } catch (Exception e) {
                throw new StarGraphException(e);
            }
        }

        return graphModel;
    }
}
