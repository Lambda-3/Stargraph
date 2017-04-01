package net.stargraph.core;

import net.stargraph.StarGraphException;
import net.stargraph.core.index.Indexer;
import net.stargraph.core.search.Searcher;
import net.stargraph.model.KBId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Encapsulates all management within a specific configured KB.
 */
public final class KBLoader {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Marker marker = MarkerFactory.getMarker("core");
    private ExecutorService executor;
    private String dbId;
    private Stargraph core;
    private boolean loading;
    private String lastResetKey;

    KBLoader(Stargraph core, String dbId) {
        this.core = Objects.requireNonNull(core);
        this.dbId = Objects.requireNonNull(dbId);
        this.executor = Executors.newSingleThreadExecutor();
        this.lastResetKey = null;
    }

    public synchronized void loadAll(String resetKey) {
        if (loading) {
            throw new StarGraphException("Loaders are in progress...");
        }

        boolean hasSomeData = core.getKBIdsOf(dbId).parallelStream().anyMatch(this::containsData);

        if (hasSomeData) {
            if (lastResetKey == null) {
                lastResetKey = UUID.randomUUID().toString();
                String msg = String.format("This KB (%s) is not empty. " +
                        "This operation WILL OVERWRITE EVERYTHING. " +
                        "Repeat this request to confirm your action adding the query param 'resetKey=%s' to the URL.", dbId, lastResetKey);
                throw new StarGraphException(msg);
            }
            else {
                if (!lastResetKey.equals(resetKey)) {
                    logger.warn(marker, "Wrong reset key='{}'", resetKey);
                    String msg = String.format("Wrong RESET KEY for KB (%s). " +
                            "Repeat this request to confirm your action adding the query param 'resetKey=%s' to the URL.", dbId, lastResetKey);
                    throw new StarGraphException(msg);
                }

                lastResetKey = null;
            }
        }

        executor.submit(() -> {
            loading = true;
            try {
                doLoadAll(dbId);
            } catch (InterruptedException e) {
                logger.error(marker, "Interrupted.", e);
            }
            finally {
                loading = false;
            }
        });
    }

    private void doLoadAll(String dbId) throws InterruptedException {
        logger.warn(marker, "Loading ALL DATA of '{}'. This can take some time ;) ..", dbId);
        List<KBId> sucessful = new ArrayList<>();
        List<KBId> failing = new ArrayList<>();
        core.getKBIdsOf(dbId).forEach(kbId -> {
            try {
                Indexer indexer = core.getIndexer(kbId);
                indexer.load(true, -1);
                indexer.awaitLoader();
                sucessful.add(kbId);
            } catch (Exception e) {
                logger.error(marker, "Fail to load {}", kbId);
                failing.add(kbId);
            }
        });

        if (failing.isEmpty()) {
            logger.info(marker, "Sucessful: {}", sucessful);
        }
        else {
            logger.warn(marker, "Sucessful: {}, failing: {}", sucessful, failing);
        }
    }

    private boolean containsData(KBId kbId) {
        Searcher searcher = core.getSearcher(kbId);
        return searcher.countDocuments() > 0;
    }
}
