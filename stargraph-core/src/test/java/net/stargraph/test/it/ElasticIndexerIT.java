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
import net.stargraph.core.Stargraph;
import net.stargraph.core.index.Indexer;
import net.stargraph.core.search.EntitySearcher;
import net.stargraph.model.*;
import net.stargraph.rank.*;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static net.stargraph.test.TestUtils.copyResource;
import static net.stargraph.test.TestUtils.createPath;

/**
 * Exercises the Elastic backend in a controlled environment.
 */
public final class ElasticIndexerIT {

    private Stargraph stargraph;
    private KBId factsId = KBId.of("obama", "facts");
    private KBId propsId = KBId.of("obama", "relations");
    private KBId entitiesId = KBId.of("obama", "entities");

    @BeforeClass
    public void before() throws Exception {
        Path root = Files.createTempFile("stargraph-", "-dataDir");
        Path hdtPath = createPath(root, factsId).resolve("triples.hdt");
        copyResource("dataSets/obama/facts/triples.hdt", hdtPath);
        ConfigFactory.invalidateCaches();
        Config config = ConfigFactory.load().getConfig("stargraph");
        stargraph = new Stargraph(config, false);
        stargraph.setDataRootDir(root.toFile());
        stargraph.initialize();
        //TODO: replace with KBLoader#loadAll()
        loadProperties(stargraph);
        loadEntities(stargraph);
        loadFacts(stargraph);
    }

    @Test
    public void classSearchTest() {
        EntitySearcher searcher = stargraph.createEntitySearcher();
        ModifiableSearchParams searchParams = ModifiableSearchParams.create("obama").term("president");
        ModifiableRankParams rankParams = ParamsBuilder.levenshtein();
        Scores scores = searcher.classSearch(searchParams, rankParams);
        ClassEntity expected = new ClassEntity("dbc:Presidents_of_the_United_States", "Presidents of the United States", true);
        Assert.assertEquals(expected, scores.get(0).getEntry());
    }

    @Test
    public void instanceSearchTest() {
        EntitySearcher searcher = stargraph.createEntitySearcher();

        ModifiableSearchParams searchParams = ModifiableSearchParams.create("obama").term("baraCk Obuma");
        ModifiableRankParams rankParams = ParamsBuilder.levenshtein(); // threshold defaults to auto
        Scores scores = searcher.instanceSearch(searchParams, rankParams);

        Assert.assertEquals(1, scores.size());
        InstanceEntity expected = new InstanceEntity("dbr:Barack_Obama", "Barack Obama");
        Assert.assertEquals(expected, scores.get(0).getEntry());
    }

    @Test
    public void propertySearchTest() {
        EntitySearcher searcher = stargraph.createEntitySearcher();

        ModifiableSearchParams searchParams = ModifiableSearchParams.create("obama").term("position");
        ModifiableRankParams rankParams = ParamsBuilder.word2vec().threshold(Threshold.auto());
        Scores scores = searcher.propertySearch(searchParams, rankParams);

        PropertyEntity expected = new PropertyEntity("dbp:office", "office");
        Assert.assertEquals(expected, scores.get(0).getEntry());
    }

    @Test
    public void pivotedSearchTest() {
        EntitySearcher searcher = stargraph.createEntitySearcher();

        ModifiableSearchParams searchParams = ModifiableSearchParams.create("obama").term("school");
        ModifiableRankParams rankParams = ParamsBuilder.word2vec().threshold(Threshold.auto());

        final InstanceEntity obama = new InstanceEntity("dbr:Barack_Obama", "Barack Obama");
        Scores scores = searcher.pivotedSearch(obama, searchParams, rankParams);

        PropertyEntity expected = new PropertyEntity("dbp:education", "education");
        Assert.assertEquals(expected, scores.get(0).getEntry());
    }

    @Test
    public void getEntitiesTest() {
        EntitySearcher searcher = stargraph.createEntitySearcher();
        LabeledEntity obama = searcher.getEntity("obama", "dbr:Barack_Obama");
        Assert.assertEquals(new InstanceEntity("dbr:Barack_Obama", "Barack Obama"), obama);
    }

    @Test
    public void getIndexerTest() throws Exception {
        Assert.assertNotNull(stargraph.getIndexer(factsId));
        Assert.assertNotNull(stargraph.getIndexer(entitiesId));
        Assert.assertNotNull(stargraph.getIndexer(propsId));
    }

    @Test(expectedExceptions = StarGraphException.class)
    public void getMissingIndexerTest() throws Exception {
        // Minimal test when multiple datasets/types are being loaded.
        Assert.assertNotNull(stargraph.getIndexer(KBId.of("mytest", "unknown")));
    }

    @Test(expectedExceptions = StarGraphException.class)
    public void getUnknownIndexerTest() {
        stargraph.getIndexer(KBId.of("unknown", "type"));
    }

    private void loadFacts(Stargraph stargraph) throws Exception {
        Indexer indexer = stargraph.getIndexer(factsId);
        indexer.load(true, -1);
        indexer.awaitLoader();
    }

    private void loadProperties(Stargraph stargraph) throws Exception {
        Indexer indexer = stargraph.getIndexer(propsId);
        indexer.load(true, -1);
        indexer.awaitLoader();
    }

    private void loadEntities(Stargraph stargraph) throws Exception {
        Indexer indexer = stargraph.getIndexer(entitiesId);
        indexer.load(true, -1);
        indexer.awaitLoader();
    }

}
