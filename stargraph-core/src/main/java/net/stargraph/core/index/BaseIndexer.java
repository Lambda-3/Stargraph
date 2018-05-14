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
import net.stargraph.data.processor.FatalProcessorException;
import net.stargraph.data.processor.Holder;
import net.stargraph.data.processor.ProcessorChain;
import net.stargraph.data.processor.ProcessorException;
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
    protected Stargraph stargraph;

    private ExecutorService loaderExecutor;
    private Future<?> loaderFutureTask;
    private ProgressWatcher loaderProgress;
    private DataProvider dataProvider;
    private ProcessorChain processorChain;
    private boolean loading;
    private boolean running;

    public BaseIndexer(KBId kbId, Stargraph stargraph) {
        this.stargraph = Objects.requireNonNull(stargraph);
        this.kbId = Objects.requireNonNull(kbId);
        this.loading = false;
        this.mapper = ObjectSerializer.createMapper(kbId);
        this.processorChain = stargraph.createProcessorChain(kbId);
    }

    @Override
    public synchronized final void start() {
        if (running) {
            throw new IllegalStateException("Already started!");
        }
        onStart();
        running = true;
    }

    @Override
    public synchronized final void stop() {
        if (!running) {
            logger.error(marker, "Indexer already stopped.");
        } else {
            try {
                onStop();
                running = false;
            }
            catch (Exception e) {
                logger.error(marker, "Fail to stop.", e);
            }

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
        doLoad(reset, limit);
    }

    @Override
    public final void flush() {
        logger.info(marker, "Flushing..");
        doFlush();
    }

    @Override
    public final void deleteAll() {
        logger.info(marker, "Deleting ALL data..");
        doDeleteAll();
        doFlush();
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

    protected void doFlush() {
        // Specific implementation detail
    }

    protected void afterLoad() throws InterruptedException {
        // Specific implementation detail
    }

    protected void doDeleteAll() {
        // Specific implementation detail
    }

    protected void onStart() {
        // Specific implementation detail
    }

    protected void onStop() {
        // Specific implementation detail
    }

    private void doBeforeLoad(boolean reset) {
        logger.debug(marker, "Before loading..");
        boolean logStats = stargraph.getMainConfig().getBoolean("progress-watcher.log-stats");
        this.loaderProgress = new ProgressWatcher(kbId, stargraph.getDataRootDir(), logStats);
        this.dataProvider = stargraph.createDataProvider(kbId);
        beforeLoad(reset);
    }

    private void doAfterLoad() throws InterruptedException {
        logger.debug(marker, ".. after loading.");
        this.loaderProgress = null;
        afterLoad();
    }

    private void sink(Holder h) {
        logger.trace(marker, "Sunk:{}", h);
    }


    private void work(Holder holder) throws ProcessorException {
        // loaderProgress may be null (if index() is called without loading)

        try {
            if (processorChain != null) {
                processorChain.run(Objects.requireNonNull(holder));
            }

            if (!holder.isSinkable()) {
                final Serializable data = holder.get();

                if (this.loading) {
                    if (loaderProgress != null && loaderProgress.getTotalIndexed() % 500000 == 0) {
                        logger.info(marker, "{}: {}", loaderProgress.getTotalIndexed(), data);
                    }
                } else {
                    // During incremental mode we log every attempt to index.
                    logger.info(marker, "Indexing {}", data);
                }

                doIndex(data, kbId);
                if (loaderProgress != null) {
                    loaderProgress.incIndexed();
                }

            } else {
                sink(holder);
            }
        } catch (FatalProcessorException e) {
            throw e;
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
                Iterator<? extends Holder> iterator = dataProvider.getIterator();
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

                    } catch (FatalProcessorException e) {
                        logger.error(marker, "Aborting.", e);
                        break;
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
                logger.info(marker, "Loader is finishing..");
                try {
                    loaderProgress.stop();
                    if (loaderProgress.getTotalIndexed() == 0) {
                        logger.warn(marker, "Nothing was loaded!");
                    }
                    doAfterLoad();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn(marker, "Thread was interrupted.", e);
                }
                finally {
                    this.loading = false;
                    logger.info(marker, "Loader is done.");
                }
            }
        });
    }

}
