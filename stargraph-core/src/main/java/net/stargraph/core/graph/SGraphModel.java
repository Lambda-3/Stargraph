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
import org.apache.jena.rdf.model.Model;
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

    private ReadWrite transactionState;
    private Model model;
    private boolean commit;

    public SGraphModel(String directory) {
        this.directory = new File(Objects.requireNonNull(directory));
        if (!this.directory.exists()) {
            this.directory.mkdirs();
        }

        this.dataset = TDBFactory.createDataset(directory);
        this.commit = true;
    }

    public boolean inReadTransaction() {
        return transactionState != null && transactionState.equals(ReadWrite.READ);
    }

    public boolean inWriteTransaction() {
        return transactionState != null && transactionState.equals(ReadWrite.WRITE);
    }

    public boolean inTransaction() {
        return inReadTransaction() || inWriteTransaction();
    }

    @Override
    public void doRead(ReadTransaction readTransaction) {
        boolean initTransaction = !inTransaction();

        if (initTransaction) {
            transactionState = ReadWrite.READ;
            dataset.begin(ReadWrite.READ);
        }

        try {
            if (initTransaction) {
                model = dataset.getDefaultModel();
            }
            if (model == null) {
                throw new AssertionError("Model should be available.");
            }

            // read-operation
            readTransaction.readTransaction(model);

        } catch (Exception e) {
            throw new StarGraphException(e);
        } finally {
            if (initTransaction) {
                dataset.end();
                model = null;
                transactionState = null;
            }
        }
    }

    @Override
    public void doWrite(WriteTransaction writeTransaction) {
        if (inReadTransaction()) {
            throw new IllegalStateException("Currently in read transaction.");
        }

        boolean initTransaction = !inTransaction();

        if (initTransaction) {
            commit = true;
            transactionState = ReadWrite.WRITE;
            dataset.begin(ReadWrite.WRITE);
        }

        try {
            if (initTransaction) {
                model = dataset.getDefaultModel();
            }
            if (model == null) {
                throw new AssertionError("Model should be available.");
            }

            // write-operation
            commit = commit && writeTransaction.writeTransaction(model);

            if (initTransaction) {
                if (commit) {
                    dataset.commit();
                } else {
                    dataset.abort();
                }
            }
        } catch (Exception e) {
            throw new StarGraphException(e);
        } finally {
            if (initTransaction) {
                dataset.end();
                model = null;
                transactionState = null;
            }
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
