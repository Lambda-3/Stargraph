package net.stargraph.core;

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
import net.stargraph.core.graph.BaseGraphModel;
import net.stargraph.core.index.Indexer;
import net.stargraph.core.search.Searcher;
import net.stargraph.data.DataProvider;
import net.stargraph.model.KBId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Encapsulates all management within a specific configured KB.
 */
public final class KBLoader {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Marker marker = MarkerFactory.getMarker("core");
    private KBCore core;

    private ExecutorService executor;
    private List<Future<?>> futures;

    private boolean loading;
    private boolean loaderScheduled;
    private boolean updating;
    private String lastResetKey;

    KBLoader(KBCore core) {
        this.core = Objects.requireNonNull(core);

        this.executor = createExecutor();
        this.futures = new ArrayList<>();

        this.loading = false;
        this.loaderScheduled = false;
        this.updating = false;

        this.lastResetKey = null;
    }

    private static ExecutorService createExecutor() {
        return Executors.newSingleThreadExecutor();
    }


    public final void await() throws InterruptedException, ExecutionException {
        await(Long.MAX_VALUE, TimeUnit.DAYS); // pretty much forever ;)
    }

    public final void await(long time, TimeUnit unit)
            throws InterruptedException, ExecutionException {
        logger.info(marker, "Awaiting finalization for {} {}..", time, unit);

        if (executor == null) {
            logger.info(marker, "Executor is already finalized.");
            return;
        }

        // shutdown
        executor.shutdown();
        if (executor.awaitTermination(time, unit)) {
            for (Future<?> future : futures) {
                future.get();
            }
        } else {
            logger.info(marker, "Time Out. Forcing finalization.");

            executor.shutdownNow();
        }
        futures.clear();
        executor = null;
    }



    private boolean containsData(KBId kbId) {
        Searcher searcher = core.getSearcher(kbId.getModel());
        return searcher.countDocuments() > 0;
    }

    public synchronized void safeLoadAll(String resetKey) {
        boolean hasSomeData = core.getKBIds().parallelStream().anyMatch(this::containsData);

        if (hasSomeData) {
            if (lastResetKey == null) {
                lastResetKey = UUID.randomUUID().toString();
                String msg = String.format("This KB (%s) is not empty. " +
                                "This operation WILL OVERWRITE EVERYTHING. " +
                                "Repeat this request to confirm your action adding the query param 'resetKey=%s' to the URL.",
                        core.getKBName(), lastResetKey);
                throw new StarGraphException(msg);
            }
            else {
                if (!lastResetKey.equals(resetKey)) {
                    logger.warn(marker, "Wrong reset key='{}'", resetKey);
                    String msg = String.format("Wrong RESET KEY for KB (%s). " +
                                    "Repeat this request to confirm your action adding the query param 'resetKey=%s' to the URL.",
                            core.getKBName(), lastResetKey);
                    throw new StarGraphException(msg);
                }

                lastResetKey = null;
            }
        }

        loadAll();
    }

    public synchronized void loadAll() {
        if (loaderScheduled) {
            throw new IllegalStateException("Loader is already scheduled.");
        }
        loaderScheduled = true;

        if (executor == null) {
            executor = createExecutor();
        }

        futures.add(
                executor.submit(() -> {
                    doLoadAll(core.getKBName());
                })
        );
    }

    private synchronized void doLoadAll(String dbId) {
        this.loading = true;

        logger.warn(marker, "##### Loading ALL DATA of '{}'. This can take some time ;) #####", dbId);
        try {
            List<KBId> successful = new ArrayList<>();
            List<KBId> failing = new ArrayList<>();

            core.getKBIds().forEach(kbId -> { // why not parallel?
                try {
                    Indexer indexer = core.getIndexer(kbId.getModel());
                    indexer.load(true, -1);
                    indexer.await();
                    successful.add(kbId);
                } catch (Exception e) {
                    logger.error(marker, "Fail to load {}", kbId);
                    failing.add(kbId);
                }
            });

            if (failing.isEmpty()) {
                logger.info(marker, "Successful: {}", successful);
            } else {
                logger.warn(marker, "Successful: {}, failing: {}", successful, failing);
            }
        } catch (Exception e) {
            logger.error(marker, "Load all failure.", e);
        } finally {
            this.loading = false;
            this.loaderScheduled = false;
        }
    }

    public synchronized void updateKB(BaseGraphModel addedModel) {
        if (loaderScheduled) {
            throw new IllegalStateException("Loader is still scheduled.");
        }

        if (executor == null) {
            executor = createExecutor();
        }

        futures.add(
                executor.submit(() -> {
                    doUpdateKB(core.getKBName(), addedModel);
                })
        );
    }

    private synchronized void doUpdateKB(String dbId, BaseGraphModel addedModel) {
        this.updating = true;

        logger.warn(marker, "##### Update graph model of '{}'. This can take some time ;) #####", dbId);

        try {
            doExtendGraphModel(dbId, addedModel);
            doUpdateIndex(dbId, addedModel);
        } catch (Exception e) {
            logger.error(marker, "Update KB failure.", e);
        } finally {
            this.updating = false;
        }
    }

    private void doExtendGraphModel(String dbId, BaseGraphModel addedModel) {
        logger.info(marker, "Extending graph model of '{}'..", dbId);

        BaseGraphModel graphModel = core.getGraphModel();
        graphModel.add(addedModel);
    }

    private void doUpdateIndex(String dbId, BaseGraphModel addedModel) {
        logger.info(marker, "Update index of '{}'..", dbId);

        core.getKBIds().forEach(kbId -> { // why not parallel?
            try {
                DataProvider dataProvider = core.getDataProvider(kbId.getModel());
                if (dataProvider.hasGraphModelUpdater()) {
                    logger.info(marker, "Incremental index-update for {}", kbId);
                    Indexer indexer = core.getIndexer(kbId.getModel());
                    indexer.update(dataProvider.getGraphModelUpdater().getIterator(addedModel));
                    indexer.await();
                } else {
                    logger.info(marker, "No graph model updater configured for {}", kbId);
                }
            } catch (Exception e) {
                logger.error(marker, "Fail to index graph model update for {}", kbId);
            }
        });
    }

}
