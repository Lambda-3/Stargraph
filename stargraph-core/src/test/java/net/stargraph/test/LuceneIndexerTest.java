package net.stargraph.test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.stargraph.core.Stargraph;
import net.stargraph.core.impl.lucene.LuceneIndexerFactory;
import net.stargraph.core.index.Indexer;
import net.stargraph.model.KBId;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

public final class LuceneIndexerTest {

    private KBId kbId;
    private Stargraph core;
    private Indexer indexer;


    @BeforeClass
    public void beforeClass() {
        ConfigFactory.invalidateCaches();
        Config config = ConfigFactory.load().getConfig("stargraph");
        File dataRootDir = TestUtils.prepareObamaTestEnv();

        this.core = new Stargraph(config, false);
        core.setDataRootDir(dataRootDir);
        this.core.setIndexerFactory(new LuceneIndexerFactory());
        this.core.initialize();
        this.kbId = KBId.of("obama", "entities");
        this.indexer = core.getIndexer(kbId);
    }


    @Test
    public void bulkLoadTest() throws Exception {
        indexer.load(true, -1);
        indexer.awaitLoader();
    }
}
