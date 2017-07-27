package net.stargraph.test.it;

import com.typesafe.config.ConfigFactory;
import net.stargraph.ModelUtils;
import net.stargraph.core.Stargraph;
import net.stargraph.core.impl.elastic.ElasticIndexer;
import net.stargraph.core.impl.elastic.ElasticSearcher;
import net.stargraph.core.index.Indexer;
import net.stargraph.data.Indexable;
import net.stargraph.model.Fact;
import net.stargraph.model.KBId;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Aims to test the incremental indexing features.
 */
public final class IndexUpdateIT {

    private Stargraph core;
    private Indexer indexer;
    private ElasticSearcher searcher;
    private KBId kbId = KBId.of("simple", "facts");

    @BeforeClass
    public void before() throws Exception {
        ConfigFactory.invalidateCaches();
        core = new Stargraph();
        searcher = new ElasticSearcher(kbId, core);
        searcher.start();
        indexer = new ElasticIndexer(kbId, core);
        indexer.start();
        indexer.deleteAll();
    }

    @Test
    public void updateTest() throws InterruptedException, TimeoutException, ExecutionException {
        Fact oneFact = ModelUtils.createFact(kbId, "dbr:Barack_Obama", "dbp:spouse", "dbr:Michelle_Obama");
        indexer.index(new Indexable(oneFact, kbId));
        indexer.flush();
        Assert.assertEquals(searcher.countDocuments(), 1);
    }
}
