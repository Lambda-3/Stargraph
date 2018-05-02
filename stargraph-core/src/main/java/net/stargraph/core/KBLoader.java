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
    private KBCore core;
    private boolean loading;
    private String lastResetKey;

    KBLoader(KBCore core) {
        this.core = Objects.requireNonNull(core);
        this.executor = Executors.newSingleThreadExecutor();
        this.lastResetKey = null;
    }

    public synchronized void loadAll(String resetKey) {
        if (loading) {
            throw new StarGraphException("Loaders are in progress...");
        }

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

        executor.submit(() -> {
            loading = true;
            try {
                doLoadAll(core.getKBName());
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
        List<KBId> successful = new ArrayList<>();
        List<KBId> failing = new ArrayList<>();
        core.getKBIds().forEach(kbId -> { // why not parallel?
            try {
                Indexer indexer = core.getIndexer(kbId.getModel());
                indexer.load(true, -1);
                indexer.awaitLoader();
                successful.add(kbId);
            } catch (Exception e) {
                logger.error(marker, "Fail to load {}", kbId);
                failing.add(kbId);
            }
        });

        if (failing.isEmpty()) {
            logger.info(marker, "Successful: {}", successful);
        }
        else {
            logger.warn(marker, "Successful: {}, failing: {}", successful, failing);
        }
    }

    private boolean containsData(KBId kbId) {
        Searcher searcher = core.getSearcher(kbId.getModel());
        return searcher.countDocuments() > 0;
    }
}
