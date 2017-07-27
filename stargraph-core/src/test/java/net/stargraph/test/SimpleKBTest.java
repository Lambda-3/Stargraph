package net.stargraph.test;

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
import net.stargraph.core.NTriplesModelFactory;
import net.stargraph.core.Stargraph;
import net.stargraph.core.index.Indexer;
import net.stargraph.core.index.NullIndicesFactory;
import net.stargraph.model.KBId;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.stargraph.test.TestUtils.copyResource;
import static net.stargraph.test.TestUtils.createPath;

public final class SimpleKBTest {
    private Stargraph core;
    private KBId factsId = KBId.of("simple", "facts");
    private KBId entitiesId = KBId.of("simple", "entities");

    @BeforeClass
    public void before() throws IOException {
        Path root = Files.createTempFile("stargraph-", "-dataDir");
        Path ntPath = createPath(root, factsId).resolve("triples.nt");
        copyResource("dataSets/simple/facts/triples.nt", ntPath);
        ConfigFactory.invalidateCaches();
        Config config = ConfigFactory.load().getConfig("stargraph");
        core = new Stargraph(config, false);
        core.setDefaultIndicesFactory(new NullIndicesFactory());
        core.setModelFactory(new NTriplesModelFactory(core));
        core.setDataRootDir(root.toFile());
        core.initialize();
    }

    @Test
    public void factLoadTest() throws Exception {
        Indexer indexer = core.getIndexer(factsId);
        indexer.load(true, -1);
        indexer.awaitLoader();
    }

    @Test
    public void entitiesLoadTest() throws Exception {
        Indexer indexer = core.getIndexer(entitiesId);
        indexer.load(true, -1);
        indexer.awaitLoader();
    }

}
