package net.stargraph.test.it;

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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.stargraph.core.KBCore;
import net.stargraph.core.KBLoader;
import net.stargraph.core.Stargraph;
import net.stargraph.core.graph.GraphModelProviderFactory;
import net.stargraph.core.graph.JModel;
import net.stargraph.core.impl.hdt.HDTModelProviderFactory;
import net.stargraph.core.impl.ntriples.NTriplesModelProviderFactory;
import net.stargraph.core.impl.turtle.TurtleModelProviderFactory;
import net.stargraph.model.KBId;
import org.apache.jena.rdf.model.*;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static net.stargraph.test.TestUtils.copyResource;
import static net.stargraph.test.TestUtils.createPath;

/**
 * Aims to test the incremental features.
 */
public final class UpdateBKIT {
    private static final JModel ADDED_MODEL;
    static {
        Model m = ModelFactory.createDefaultModel();
        Resource instance1 = m.createResource("http://dbpedia.org/resource/Barack_Obama");
        Resource instance2 = m.createResource("http://dbpedia.org/resource/Michelle_Obama");
        Property property = ResourceFactory.createProperty("http://dbpedia.org/property/commanderInChief");
        instance1.addProperty(property, instance2);
        ADDED_MODEL = new JModel(m);
    }


    private Stargraph stargraph;
    private KBCore core;
    private KBLoader loader;
    private KBId kbId = KBId.of("simple", "facts");

    private void setup(String type) throws IOException {
        Path root = Files.createTempFile("stargraph-", "-dataDir");

        Path path;
        switch (type) {
            case "hdt":
                path = createPath(root, kbId).resolve("triples.hdt");
                copyResource("dataSets/simple/facts/triples.hdt", path);
                break;
            case "nt":
                path = createPath(root, kbId).resolve("triples.nt");
                copyResource("dataSets/simple/facts/triples.nt", path);
                break;
            case "ttl":
                path = createPath(root, kbId).resolve("triples.ttl");
                copyResource("dataSets/simple/facts/triples.ttl", path);
                break;
            default:
                throw new AssertionError("Unknown type");
        }

        ConfigFactory.invalidateCaches();
        Config config = ConfigFactory.load().getConfig("stargraph");
        stargraph = new Stargraph(config, false);
        stargraph.setDataRootDir(root.toFile());

        GraphModelProviderFactory factory;
        switch (type) {
            case "hdt":
                factory = new HDTModelProviderFactory(stargraph);
                break;
            case "nt":
                factory = new NTriplesModelProviderFactory(stargraph);
                break;
            case "ttl":
                factory = new TurtleModelProviderFactory(stargraph);
                break;
            default:
                throw new AssertionError("Unknown type");
        }
        stargraph.setDefaultGraphModelProviderFactory(factory);

        stargraph.initialize();
        core = stargraph.getKBCore("simple");
        loader = core.getLoader();
    }

    private void updateTest(String type) throws InterruptedException, ExecutionException, IOException {
        setup(type);

        loader.loadAll();
        loader.await();

        long graphSizeBefore = core.getGraphModel().size();
        long factsSizeBefore = core.getSearcher("facts").countDocuments();
        long entitiesSizeBefore = core.getSearcher("entities").countDocuments();

        loader.updateKB(ADDED_MODEL);
        loader.await();

        long graphSizeAfter = core.getGraphModel().size();
        long factsSizeAfter = core.getSearcher("facts").countDocuments();
        long entitiesSizeAfter = core.getSearcher("entities").countDocuments();

        Assert.assertTrue(graphSizeAfter > graphSizeBefore);
        Assert.assertTrue(factsSizeAfter > factsSizeBefore);
        Assert.assertTrue(entitiesSizeAfter > entitiesSizeBefore);
    }

    @BeforeClass
    public void before() throws Exception {
        setup("nt");
    }

    @Test
    public void updateHDTTest() throws Exception {
        updateTest("hdt");
    }

    @Test
    public void updateNTriplesTest() throws Exception {
        updateTest("nt");
    }

    @Test
    public void updateTurtleTest() throws Exception {
        updateTest("ttl");
    }


    @Test(timeOut = 15000)
    public void loadAll() throws Exception {
        loader.loadAll();
        loader.await();
    }

    @Test
    public void loadTwiceTest() throws Exception {
        loader.loadAll();
        loader.await();
        loader.loadAll();
        loader.await();
    }

    @Test
    public void loadAndUpdateTest() throws Exception {
        loader.loadAll();
        loader.await();
        loader.updateKB(ADDED_MODEL);
        loader.await();
    }

    @Test
    public void loadAndDoubleUpdateTest() throws Exception {
        loader.loadAll();
        loader.await();
        loader.updateKB(ADDED_MODEL);
        loader.updateKB(ADDED_MODEL);
        loader.await();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void loadingWhileLoadingTest() throws Exception {
        try {
            loader.loadAll();
            loader.loadAll();
        }
        finally {
            loader.await();
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void updatingWhileLoadingTest() throws Exception {
        try {
            loader.loadAll();
            loader.updateKB(ADDED_MODEL);
        } finally {
            loader.await();
        }
    }
}
