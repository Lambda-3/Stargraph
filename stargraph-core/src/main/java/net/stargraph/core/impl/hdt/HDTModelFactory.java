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

import com.typesafe.config.Config;
import net.stargraph.StarGraphException;
import net.stargraph.core.GraphModelFactory;
import net.stargraph.core.Stargraph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class HDTModelFactory extends GraphModelFactory {

    public HDTModelFactory(Stargraph core) {
        super(core);
    }

    @Override
    protected Model createModel(String dbId) {
        if (!hasTripleStore(dbId)) {
            logger.warn(marker, "Can't create Graph Model from {}. No triple-store configured.", dbId);
        } else {
            try {
                File hdtFile = getHDTPath(dbId).toFile();
                if (hdtFile.exists()) {
                    boolean hasIdx = hasIndex(dbId);
                    logger.info(marker, "Using HDT index? {}", hasIdx);
                    String hdtFilePathStr = hdtFile.getAbsolutePath();
                    logger.trace(marker, "HDT: '{}'", hdtFilePathStr);
                    HDT hdt = hasIdx ? HDTManager.mapIndexedHDT(hdtFilePathStr, null) : HDTManager.loadHDT(hdtFilePathStr, null);
                    HDTGraph graph = new HDTGraph(hdt);
                    return ModelFactory.createModelForGraph(graph);
                }
                logger.error(marker, "HDT file not found: {}", hdtFile);
            } catch (Exception e) {
                throw new StarGraphException(e);
            }
        }

        return null;
    }

    private boolean hasIndex(String id) {
        Config tripleStoreCfg = core.getKBConfig(id).getConfig("triple-store");
        return tripleStoreCfg.hasPath("hdt.use-index") && tripleStoreCfg.getBoolean("hdt.use-index");
    }

    private Path getHDTPath(String dbId) {
        Config mainConfig = core.getConfig();
        String dataDir = mainConfig.getString("data.root-dir");
        return Paths.get(dataDir, dbId, "facts", "triples.hdt");
    }

}
