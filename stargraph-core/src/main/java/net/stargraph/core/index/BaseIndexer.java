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
import java.util.*;
import java.util.concurrent.*;

/**
 * Full state default indexer.
 */
public abstract class BaseIndexer implements Indexer {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected Marker marker = MarkerFactory.getMarker("index");
    protected Stargraph stargraph;
    protected KBId kbId;
    protected ObjectMapper mapper;

    private ExecutorService executor;
    private List<Future<?>> futures;

    private ProcessorChain processorChain;

    private boolean running;
    private boolean loading;
    private boolean loaderScheduled;
    private boolean updating;

    public BaseIndexer(KBId kbId, Stargraph stargraph) {
        this.stargraph = Objects.requireNonNull(stargraph);
        this.kbId = Objects.requireNonNull(kbId);
        this.mapper = ObjectSerializer.createMapper(kbId);

        this.executor = createExecutor();
        this.futures = new ArrayList<>();

        this.processorChain = stargraph.createProcessorChain(kbId);

        this.running = false;
        this.loading = false;
        this.loaderScheduled = false;
        this.updating = false;
    }

    private static ExecutorService createExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    @Override
    public synchronized final void start() {
        if (running) {
            throw new IllegalStateException("Already started!");
        }
        this.running = true;
        this.loading = false;
        this.loaderScheduled = false;
        this.updating = false;
        onStart();
    }

    @Override
    public synchronized final void stop() {
        if (!running) {
            logger.error(marker, "Indexer already stopped.");
        } else {
            try {
                onStop();
                await();
                running = false;
            }
            catch (Exception e) {
                logger.error(marker, "Fail to stop.", e);
            }
        }
    }

    @Override
    public void update(Indexable data) throws InterruptedException {
        update(Arrays.asList(data).iterator());
    }

    @Override
    public void update(Iterator<Indexable> data) throws InterruptedException {
        update(data, -1);
    }

    @Override
    public void update(Iterator<Indexable> data, long limit) throws InterruptedException {
        doUpdate(data, limit);
    }

    @Override
    public final void load() {
        load(false, -1);
    }

    @Override
    public final void load(boolean reset, long limit) {
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
    public final void await() throws InterruptedException, ExecutionException {
        await(Long.MAX_VALUE, TimeUnit.DAYS); // pretty much forever ;)
    }

    @Override
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


    protected abstract void beforeLoad(boolean reset);

    protected abstract void doIndex(Serializable data, KBId kbId) throws InterruptedException;

    protected void doFlush() {
        // Specific implementation detail
    }

    protected void afterLoad() throws InterruptedException {
        // Specific implementation detail
    }

    protected void beforeUpdate() {
        // Specific implementation detail
    }

    protected void afterUpdate() {
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
        if (reset) {
            logger.warn(marker, "Old data will be DELETED!");
        }
        beforeLoad(reset);
    }

    private void doAfterLoad() throws InterruptedException {
        logger.debug(marker, "After loading..");
        afterLoad();
    }

    private void doBeforeUpdate() {
        logger.debug(marker, "Before incremental updating..");
        beforeUpdate();
    }

    private void doAfterUpdate() throws InterruptedException {
        logger.debug(marker, "After incremental updating..");
        afterUpdate();
    }













    private synchronized void doLoad(boolean reset, long limit) {
        if (loaderScheduled) {
            throw new IllegalStateException("Loader is already scheduled.");
        }
        loaderScheduled = true;

        if (executor == null) {
            executor = createExecutor();
        }

        logger.info(marker, "Queue new loading task for {}", kbId);

        DataProvider dataProvider = stargraph.getKBCore(kbId.getId()).getDataProvider(kbId.getModel());
        Iterator dataIt = dataProvider.getMergedDataSource().createIterator();
        futures.add(
                executor.submit(() -> {
                    runnableTask(dataIt, limit, true, reset);
                })
        );
    }

    private synchronized void doUpdate(Iterator<Indexable> dataIt, long limit) {
        if (loaderScheduled) {
            throw new IllegalStateException("Loader is still scheduled.");
        }

        if (executor == null) {
            executor = createExecutor();
        }

        logger.info(marker, "Queue new incremental update task for {}", kbId);
        futures.add(
                executor.submit(() -> {
                    runnableTask(dataIt, limit, false, false);
                })
        );
    }

    private synchronized void runnableTask(Iterator<Indexable> iterator, long limit, boolean useLoader, boolean reset) {
        String label = (useLoader)? "Loader" : "Incremental Update";

        if (useLoader) {
            this.loading = true;
        } else {
            this.updating = true;
        }

        logger.info(marker, "### Started {} task for {}, [reset={}, limit={}] ###", label, kbId, reset, limit);

        boolean logStats = stargraph.getMainConfig().getBoolean("progress-watcher.log-stats");
        ProgressWatcher progressWatcher = new ProgressWatcher(kbId, stargraph.getDataRootDir(), logStats);

        try {
            progressWatcher.start(true);
            logger.info(marker, label + " is running..");

            if (useLoader) {
                doBeforeLoad(reset);
            } else {
                doBeforeUpdate();
            }

            while (iterator.hasNext()) {
                if (limit > 0 && progressWatcher.getTotalRead() >= limit) {
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

                    processAndIndex(data, progressWatcher); // delegates heavy work

                } catch (FatalProcessorException e) {
                    logger.error(marker, "Aborting.", e);
                    break;
                } catch (Exception e) {
                    logger.error(marker, "Error reading data.", e);
                } finally {
                    progressWatcher.incRead();
                }
            }
        } catch (Exception e) {
            logger.error(marker, label + " failure.", e);
            throw e;
        } finally {
            logger.info(marker, label + " is finishing..");
            try {
                progressWatcher.stop();
                if (progressWatcher.getTotalIndexed() == 0) {
                    logger.warn(marker, "Nothing was indexed!");
                }
                if (useLoader) {
                    doAfterLoad();
                } else {
                    doAfterUpdate();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn(marker, "Thread was interrupted.", e);
            }
            finally {
                if (useLoader) {
                    this.loading = false;
                    this.loaderScheduled = false;
                } else {
                    this.updating = false;
                }
                logger.info(marker, "### {} is done. ###", label);
            }
        }
    }

    private void processAndIndex(Holder holder, ProgressWatcher progressWatcher) throws ProcessorException {
        try {
            if (processorChain != null) {
                processorChain.run(Objects.requireNonNull(holder));
            }

            if (!holder.isSinkable()) {
                final Serializable data = holder.get();

                if (this.loading) {
                    if (progressWatcher.getTotalIndexed() % 500000 == 0) {
                        logger.info(marker, "{}: {}", progressWatcher.getTotalIndexed(), data);
                    }
                }
                if (this.updating) {
                    // During incremental mode we log every attempt to index.
                    logger.info(marker, "Indexing {}", data);
                }

                doIndex(data, kbId);
                progressWatcher.incIndexed();

            } else {
                logger.trace(marker, "Sunk:{}", holder);
            }
        } catch (FatalProcessorException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error(marker, "Fail to process {}", holder, e);
        }
    }

}
