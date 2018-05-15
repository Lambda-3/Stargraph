package net.stargraph.core.impl.hdt;

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
import net.stargraph.core.graph.JModel;
import net.stargraph.core.graph.DefaultModelFileLoader;
import org.apache.jena.rdf.model.ModelFactory;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;

import java.io.File;

public final class HDTModelFileLoader extends DefaultModelFileLoader {
    private final boolean useIndex;


    public HDTModelFileLoader(String dbid, File file, boolean useIndex) {
        super(dbid, file, null);
        this.useIndex = useIndex;
    }

    @Override
    public JModel loadModel() {
        logger.info(marker, "Loading '{}', useIndex={}", file.getAbsolutePath(), useIndex);

        JModel model = null;

        try {
            String hdtFilePathStr = file.getAbsolutePath();
            HDT hdt = useIndex ? HDTManager.mapIndexedHDT(hdtFilePathStr, null) : HDTManager.loadHDT(hdtFilePathStr, null);
            HDTGraph graph = new HDTGraph(hdt);
            model = new JModel(ModelFactory.createModelForGraph(graph));
        } catch (Exception e) {
            throw new StarGraphException(e);
        } finally {
            if (model == null) {
                logger.error(marker, "No Graph Model instantiated for {}", dbId);
            }
        }

        return model;
    }
}
