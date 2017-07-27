package net.stargraph.test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.stargraph.core.Stargraph;
import net.stargraph.core.index.Indexer;
import net.stargraph.core.search.Searcher;
import net.stargraph.model.KBId;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

public final class LuceneIndexerTest {

    private KBId kbId = KBId.of("obama", "entities"); // Entities uses Lucene. See reference.conf.
    private Stargraph core;


    @BeforeClass
    public void beforeClass() {
        ConfigFactory.invalidateCaches();
        Config config = ConfigFactory.load().getConfig("stargraph");
        File dataRootDir = TestUtils.prepareObamaTestEnv().toFile();
        this.core = new Stargraph(config, false);
        core.setKBInitSet(kbId.getId());
        core.setDataRootDir(dataRootDir);
        this.core.initialize();
    }


    @Test
    public void bulkLoadTest() throws Exception {
        Indexer indexer = core.getIndexer(kbId);
        indexer.load(true, -1);
        indexer.awaitLoader();
        Searcher searcher = core.getSearcher(kbId);
        Assert.assertEquals(searcher.countDocuments(), 756);
    }
}
