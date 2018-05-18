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

import net.stargraph.model.GraphModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.AddDeniedException;

import java.io.IOException;

/**
 * A wrapper for Jena's graph model
 */
public abstract class BaseGraphModel implements GraphModel {
    public interface ReadTransaction {
        void readTransaction(Model model);
    }

    public interface WriteTransaction {
        boolean writeTransaction(Model model) throws IOException;
    }

    public void add(BaseGraphModel other) {
        doWrite(new WriteTransaction() {
            @Override
            public boolean writeTransaction(Model thisModel) {
                other.doRead(new ReadTransaction() {
                    @Override
                    public void readTransaction(Model otherModel) {
                        thisModel.add(otherModel);
                    }
                });

                return true;
            }
        });
    }

    public long getSize() {
        final long[] size = {0};

        doRead(new ReadTransaction() {
            @Override
            public void readTransaction(Model model) {
                size[0] = model.size();
            }
        });

        return size[0];
    }

    public abstract void doRead(ReadTransaction readTransaction);
    public abstract void doWrite(WriteTransaction writeTransaction);
    public abstract void reset();
    public abstract void close();
}
