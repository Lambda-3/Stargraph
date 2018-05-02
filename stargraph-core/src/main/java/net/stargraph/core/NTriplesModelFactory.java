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
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class NTriplesModelFactory extends GraphModelFactory {

    public NTriplesModelFactory(Stargraph core) {
        super(core);
    }

    @Override
    protected Model createModel(String dbId) {
        File ntriplesFile = getNTriplesPath(dbId).toFile();
        if (!ntriplesFile.exists()) {
            logger.warn(marker, "Can't find NT file {}", ntriplesFile);
        } else {

            try (InputStream is = new FileInputStream(ntriplesFile)) {
                Model model = ModelFactory.createDefaultModel();
                model.read(is, null, "N-TRIPLES");
                return model;
            } catch (Exception e) {
                throw new StarGraphException(e);
            }
        }

        return null;
    }

    private Path getNTriplesPath(String dbId) {
        return Paths.get(stargraph.getDataRootDir(), dbId, "facts", "triples.nt");
    }
}
