package net.stargraph.core.impl.ntriples;

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
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;

public final class NTriplesModelFileLoader {
    private static Logger logger = LoggerFactory.getLogger(NTriplesModelFileLoader.class);
    private static Marker marker = MarkerFactory.getMarker("core");

    private final String dbId;
    private final File file;


    public NTriplesModelFileLoader(String dbid, File file) {
        this.dbId = dbid;
        this.file = file;
    }

    public JModel loadModel() {
        logger.info(marker, "Loading '{}'", file.getAbsolutePath());

        JModel model = null;

        try (InputStream is = new FileInputStream(file)) {
            Model m = ModelFactory.createDefaultModel();
            m.read(is, null, "N-TRIPLES");
            model = new JModel(m);
        } catch (Exception e) {
            throw new StarGraphException(e);
        } finally {
            if (model == null) {
                logger.error(marker, "No Graph Model instantiated for {}", dbId);
            }
        }

        return model;
    }

    public Iterator<JModel> loadModelAsIterator() {
        return Arrays.asList(loadModel()).iterator();
    }
}
