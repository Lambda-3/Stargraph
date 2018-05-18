package net.stargraph.core.graph;

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
import org.apache.commons.io.FileUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.tdb.TDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * A model stored in the file system.
 */
public class SGraphModel extends BaseGraphModel {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected Marker marker = MarkerFactory.getMarker("graph");

    private final File directory;
    private Dataset dataset;

    public SGraphModel(String directory, boolean reset) {
        this.directory = new File(Objects.requireNonNull(directory));
        if (!this.directory.exists()) {
            this.directory.mkdirs();
        }

        if (reset) {
            reset();
        }

        this.dataset = TDBFactory.createDataset(directory);
    }

    @Override
    public void doRead(ReadTransaction readTransaction) {
        dataset.begin(ReadWrite.READ) ;
        try {
            readTransaction.readTransaction(dataset.getDefaultModel());
        } catch (Exception e) {
            throw new StarGraphException(e);
        } finally {
            dataset.end() ;
        }
    }

    @Override
    public void doWrite(WriteTransaction writeTransaction) {
        dataset.begin(ReadWrite.WRITE) ;
        try {
            if (writeTransaction.writeTransaction(dataset.getDefaultModel())) {
                dataset.commit();
            } else {
                dataset.abort();
            }
        } catch (Exception e) {
            throw new StarGraphException(e);
        } finally {
            dataset.end() ;
        }
    }

    @Override
    public void reset() {
        logger.info(marker, "Reset graph model");

        try {
            if (directory.exists()) {
                FileUtils.deleteDirectory(directory.getAbsoluteFile());
                if (directory.exists()) {
                    throw new StarGraphException("Directory should be gone.");
                }
            }
            directory.mkdirs();
        } catch (IOException e) {
            logger.error(marker, "Failed to reset graph model");
            throw new StarGraphException(e);
        } finally {
            close();
        }

        this.dataset = TDBFactory.createDataset(directory.getAbsolutePath());
    }

    public void close() {
        if (dataset != null) {
            dataset.close();
            dataset = null;
        }
    }
}
