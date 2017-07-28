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

import net.stargraph.model.KBId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


public final class ProgressWatcher {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Marker marker = MarkerFactory.getMarker("watcher");
    private AtomicLong read = new AtomicLong(0);
    private AtomicLong indexed = new AtomicLong(0);
    private long startTime;
    private long stopTime;
    private long elapsedTime;
    private ScheduledExecutorService executor;
    private KBId kbId;
    private boolean logStats;
    private String dataRootDir;


    public ProgressWatcher(KBId kbId, String dataRootDir, boolean logStats) {
        this.kbId = Objects.requireNonNull(kbId);
        this.dataRootDir = Objects.requireNonNull(dataRootDir);
        this.logStats = logStats;
    }

    public long incRead() {
        return read.incrementAndGet();
    }

    public long incIndexed() {
        return indexed.incrementAndGet();
    }

    public long getTotalIndexed() {
        return indexed.get();
    }

    public long getTotalRead() {
        return read.get();
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public synchronized void stop() throws InterruptedException {
        if (executor != null && !executor.isShutdown()) {
            stopTime = System.nanoTime() / 1000_000;
            elapsedTime = stopTime - startTime;
            executor.shutdownNow();
            logStats();
            logger.info(marker, "{}", getReportMsg());
        }
    }

    public synchronized void start(boolean reset) {
        if (executor == null || executor.isTerminated()) {
            executor = Executors.newSingleThreadScheduledExecutor();
        } else {
            throw new IllegalStateException();
        }

        if (reset) {
            read.set(0);
            indexed.set(0);
            startTime = System.nanoTime() / 1000_000;
            stopTime = 0;
            elapsedTime = 0;
        }

        executor.scheduleAtFixedRate(() -> {
            elapsedTime = (System.nanoTime() / 1000_000) - startTime;
            if (elapsedTime > 0) {
                double entriesPerSec = 1000 * getTotalRead() / elapsedTime;
                logger.info(marker, "{} entries/s. {}", entriesPerSec, getReportMsg());
                logger.info(marker, "Memory: {}", ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());
            }
        }, 10, 10, TimeUnit.SECONDS);

        logger.info(marker, "Progress Watcher started.");
    }

    public String getReportMsg() {
        long elapsedTime = getElapsedTime();
        return String.format("Read %d entries in %d min, %d sec. Indexed %d entries.",
                getTotalRead(),
                TimeUnit.MILLISECONDS.toMinutes(elapsedTime),
                TimeUnit.MILLISECONDS.toSeconds(elapsedTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTime)), getTotalIndexed());
    }

    private void logStats() {
        if (logStats) {
            File csvFile = Paths.get(dataRootDir, kbId.getId(), String.format("indexing-time-%s.csv", kbId.getModel())).toFile();
            logger.info(marker, "Logging stats to {}", csvFile);

            boolean exists = csvFile.exists();
            try (FileWriter writer = new FileWriter(csvFile, exists)) {
                if (!exists) {
                    writer.write("read,indexed,elapsed(ms)\n"); //header
                }
                writer.append(String.format("%d,%d,%d\n", getTotalRead(), getTotalIndexed(), getElapsedTime()));
            } catch (IOException e) {
                logger.error(marker, "Fail to log stats", e);
            }
        }
        else {
            logger.debug(marker, "Stats will not be persisted, stargraph.progress-watcher.log-stats=no.");
        }
    }
}
