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
import net.stargraph.StarGraphException;
import net.stargraph.core.KBCore;
import net.stargraph.core.Stargraph;
import net.stargraph.core.index.Indexer;
import net.stargraph.core.search.EntitySearcher;
import net.stargraph.model.*;
import net.stargraph.rank.*;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static net.stargraph.test.TestUtils.copyResource;
import static net.stargraph.test.TestUtils.createGraphModelPath;

/**
 *
 */
public final class IndexerIT {

    private KBCore core;
    private EntitySearcher entitySearcher;
    private String dbId = "obama";
    private KBId factsId = KBId.of("obama", "facts");
    private KBId propsId = KBId.of("obama", "relations");
    private KBId entitiesId = KBId.of("obama", "entities");

    @BeforeClass
    public void before() throws Exception {
        Path root = Files.createTempFile("stargraph-", "-dataDir");
        Path hdtPath = createGraphModelPath(root, dbId).resolve("triples.hdt");
        copyResource("dataSets/obama/graph/triples.hdt", hdtPath);
        ConfigFactory.invalidateCaches();
        Config config = ConfigFactory.load().getConfig("stargraph");
        Stargraph stargraph = new Stargraph(config, false);
        stargraph.setDataRootDir(root.toFile());
        stargraph.initialize();
        core = stargraph.getKBCore("obama");
        entitySearcher = stargraph.getEntitySearcher();
        //TODO: replace with KBLoader#loadAll()
        loadProperties();
        loadEntities();
        loadFacts();
    }

    @Test
    public void classSearchTest() {
        ModifiableSearchParams searchParams = ModifiableSearchParams.create("obama").term("president");
        ModifiableRankParams rankParams = ParamsBuilder.levenshtein();
        Scores scores = entitySearcher.classSearch(searchParams, rankParams);
        ClassEntity expected = new ClassEntity("dbc:Presidents_of_the_United_States", "Presidents of the United States", true);
        Assert.assertEquals(expected, scores.get(0).getEntry());
    }

    @Test
    public void instanceSearchTest() {
        ModifiableSearchParams searchParams = ModifiableSearchParams.create("obama").term("baraCk Obuma");
        ModifiableRankParams rankParams = ParamsBuilder.levenshtein(); // threshold defaults to auto
        Scores scores = entitySearcher.instanceSearch(searchParams, rankParams);

        Assert.assertEquals(1, scores.size());
        InstanceEntity expected = new InstanceEntity("dbr:Barack_Obama", "Barack Obama");
        Assert.assertEquals(expected, scores.get(0).getEntry());
    }

    @Test
    public void propertySearchTest() {
        ModifiableSearchParams searchParams = ModifiableSearchParams.create("obama").term("position");
        ModifiableRankParams rankParams = ParamsBuilder.word2vec().threshold(Threshold.auto());
        Scores scores = entitySearcher.propertySearch(searchParams, rankParams);

        PropertyEntity expected = new PropertyEntity("dbp:office", "office");
        Assert.assertEquals(expected, scores.get(0).getEntry());
    }

    @Test
    public void pivotedSearchTest() {
        ModifiableSearchParams searchParams = ModifiableSearchParams.create("obama").term("school");
        ModifiableRankParams rankParams = ParamsBuilder.word2vec().threshold(Threshold.auto());

        final InstanceEntity obama = new InstanceEntity("dbr:Barack_Obama", "Barack Obama");
        Scores scores = entitySearcher.pivotedSearch(obama, searchParams, rankParams);

        PropertyEntity expected = new PropertyEntity("dbp:education", "education");
        Assert.assertEquals(expected, scores.get(0).getEntry());
    }

    @Test
    public void getEntitiesTest() {
        LabeledEntity obama = entitySearcher.getEntity("obama", "dbr:Barack_Obama");
        Assert.assertEquals(new InstanceEntity("dbr:Barack_Obama", "Barack Obama"), obama);
    }

    @Test
    public void getIndexerTest() throws Exception {
        Assert.assertNotNull(core.getIndexer(factsId.getModel()));
        Assert.assertNotNull(core.getIndexer(entitiesId.getModel()));
        Assert.assertNotNull(core.getIndexer(propsId.getModel()));
    }

    @Test(expectedExceptions = StarGraphException.class)
    public void getUnknownIndexerTest() {
        core.getIndexer("unknown");
    }

    private void loadFacts() throws Exception {
        Indexer indexer = core.getIndexer(factsId.getModel());
        indexer.load(true, -1);
        indexer.await();
    }

    private void loadProperties() throws Exception {
        Indexer indexer = core.getIndexer(propsId.getModel());
        indexer.load(true, -1);
        indexer.await();
    }

    private void loadEntities() throws Exception {
        Indexer indexer = core.getIndexer(entitiesId.getModel());
        indexer.load(true, -1);
        indexer.await();
    }

}
