package net.stargraph.core.index;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import net.stargraph.StarGraphException;
import net.stargraph.core.Stargraph;
import net.stargraph.core.serializer.ObjectSerializer;
import net.stargraph.data.DataProvider;
import net.stargraph.data.Indexable;
import net.stargraph.data.processor.Holder;
import net.stargraph.data.processor.ProcessorChain;
import net.stargraph.model.KBId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * Full state default indexer.
 */
public abstract class BaseIndexer implements Indexer {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected Marker marker = MarkerFactory.getMarker("index");
    protected KBId kbId;
    protected ObjectMapper mapper;
    protected Stargraph core;

    private ExecutorService loaderExecutor;
    private Future<?> loaderFutureTask;
    private ProgressWatcher loaderProgress;
    private boolean loading;
    private DataProvider<?> dataProvider;
    private ProcessorChain processorChain;

    public BaseIndexer(KBId kbId, Stargraph core) {
        logger.info(marker, "Initializing {}", kbId);
        this.core = Objects.requireNonNull(core);
        this.kbId = Objects.requireNonNull(kbId);
        this.loading = false;
        this.mapper = ObjectSerializer.createMapper(kbId);
    }

    @Override
    public synchronized final void start() {
        if (dataProvider != null) {
            throw new IllegalStateException("Already started!");
        }
        this.loaderProgress = new ProgressWatcher(kbId, core.getConfig());
        onStart();
    }

    @Override
    public synchronized final void stop() {
        if (dataProvider == null) {
            logger.warn(marker, "Is stopped.");
        } else {
            onStop();
        }
    }

    @Override
    public final void index(Indexable data) throws InterruptedException {
        if (loading) {
            throw new IllegalStateException("Loader in progress. Incremental update is forbidden.");
        }

        work(data);
    }

    @Override
    public final void load() {
        load(false, -1);
    }

    @Override
    public final void load(boolean reset, int limit) {
        try {
            doLoad(reset, limit);
        } catch (Exception e) {
            throw new StarGraphException(e);
        }
    }

    @Override
    public final void awaitLoader() throws InterruptedException, TimeoutException, ExecutionException {
        awaitLoader(Long.MAX_VALUE, TimeUnit.DAYS); // pretty much forever ;)
    }

    @Override
    public final void awaitLoader(long time, TimeUnit unit)
            throws InterruptedException, TimeoutException, ExecutionException {

        if (!loading && loaderFutureTask == null) {
            throw new IllegalStateException("Loader was not called.");
        }

        logger.info(marker, "Awaiting Loader finalization..");
        loaderExecutor.shutdown();
        if (!loaderExecutor.awaitTermination(time, unit)) {
            logger.info(marker, "Time Out. Forcing termination.");
            loaderExecutor.shutdownNow();
        } else {
            loaderFutureTask.get();
        }

    }

    protected abstract void beforeLoad(boolean reset);

    protected abstract void doIndex(Serializable data, KBId kbId) throws InterruptedException;

    protected void afterLoad() throws InterruptedException {
        // Specific implementation detail
    }

    protected void onStart() {
        // Specific implementation detail
    }

    protected void onStop() {
        // Specific implementation detail
    }

    private void doBeforeLoad(boolean reset) {
        logger.info(marker, "Before loading..");
        dataProvider = core.createDataProvider(kbId);
        this.processorChain = core.createProcessorChain(kbId);
        beforeLoad(reset);
    }

    private void doAfterLoad() throws InterruptedException {
        logger.info(marker, ".. after loading.");
        afterLoad();
    }

    private void sink(Holder h) {
        logger.trace(marker, "Sunk:{}", h);
    }


    private void work(Holder holder) {
        try {
            if (processorChain != null) {
                processorChain.run(Objects.requireNonNull(holder));
            }

            if (!holder.isSinkable()) {
                Serializable data = holder.get();

                if (this.loading) {
                    if (loaderProgress.incIndexed() % 500000 == 0) {
                        logger.info(marker, "{}: {}", loaderProgress.getTotalIndexed(), data);
                    }
                } else {
                    // During incremental mode we log every attempt to index.
                    logger.info(marker, "Indexing {}", data);
                }

                doIndex(holder.get(), kbId);

            } else {
                sink(holder);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error(marker, "Fail to process {}", holder, e);
        }
    }

    private synchronized void doLoad(boolean reset, long limit) {
        if (loading) {
            throw new IllegalStateException("Loader is already in progress. ");
        }

        logger.info(marker, "Loading {}, [reset={}, limit={}]", kbId, reset, limit);
        loading = true;

        if (reset) {
            logger.warn(marker, "Old data will be DELETED!");
        }

        if (loaderExecutor == null || loaderExecutor.isTerminated()) {
            this.loaderExecutor = Executors.newSingleThreadExecutor();
        }

        loaderFutureTask = loaderExecutor.submit(() -> {
            try {
                doBeforeLoad(reset);
                loaderProgress.start(true); // now this is always true until we add a resume feature.
                logger.info(marker, "Loader is running..");
                Iterator<? extends Holder> iterator = dataProvider.iterator();
                while (iterator.hasNext()) {

                    if (limit > 0 && loaderProgress.getTotalRead() >= limit) {
                        logger.info(marker, "Limit set to {} reached.", limit);
                        break;
                    }

                    try {
                        Holder data = iterator.next();
                        if (Thread.currentThread().isInterrupted()) {
                            break;
                        }

                        if (!data.getKBId().equals(kbId)) {
                            throw new StarGraphException("Can't consume data from '{}" + data.getKBId() + "'");
                        }

                        work(data); // delegates heavy work

                    } catch (Exception e) {
                        logger.error(marker, "Error reading from provider.", e);
                    } finally {
                        loaderProgress.incRead();
                    }
                }
            } catch (Exception e) {
                logger.error(marker, "Loader failure.", e);
                throw e;
            } finally {
                try {
                    doAfterLoad();
                    loaderProgress.stop();
                    if (loaderProgress.getTotalIndexed() == 0) {
                        logger.warn(marker, "Nothing was loaded!");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn(marker, "Thread was interrupted.", e);
                }

                this.loading = false;
                logger.info(marker, "Loader is done.");
            }
        });
    }

}
