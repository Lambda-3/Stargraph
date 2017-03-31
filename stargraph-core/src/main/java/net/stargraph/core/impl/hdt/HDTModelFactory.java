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

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class HDTModelFactory extends GraphModelFactory {

    public HDTModelFactory(Stargraph core) {
        super(core);
    }

    @Override
    protected Model createModel(String dbId) {
        try {
            File hdtFile = getHDTPath(dbId).toFile();
            if (hdtFile.exists()) {
                boolean useIdx = useIndex(dbId);
                logger.info(marker, "Using HDT index? {}", useIdx);
                String hdtFilePathStr = hdtFile.getAbsolutePath();
                logger.trace(marker, "HDT: '{}'", hdtFilePathStr);
                HDT hdt = useIdx ? HDTManager.mapIndexedHDT(hdtFilePathStr, null) : HDTManager.loadHDT(hdtFilePathStr, null);
                HDTGraph graph = new HDTGraph(hdt);
                return ModelFactory.createModelForGraph(graph);
            }

           throw new FileNotFoundException("HDT file not found: '" + hdtFile + "'");

        } catch (Exception e) {
            throw new StarGraphException(e);
        }
    }

    private boolean useIndex(String id) {
        Config tripleStoreCfg = core.getKBConfig(id).getConfig("triple-store");
        return tripleStoreCfg.hasPath("hdt.use-index") && tripleStoreCfg.getBoolean("hdt.use-index");
    }

    private Path getHDTPath(String dbId) throws IOException {

        Config mainConfig = core.getConfig();
        String dataDir = mainConfig.getString("data.root-dir");
        Path defaultPath = Paths.get(dataDir, dbId, "facts", "triples.hdt");

        final String cfgPath = "triple-store.hdt.file";
        Config cfg = core.getKBConfig(dbId);

        if (cfg.hasPath(cfgPath)) {
            String hdtFileName = cfg.getString(cfgPath);
            if (hdtFileName == null || hdtFileName.isEmpty()) {
                throw new StarGraphException("Invalid configuration at '" + cfgPath + "'");
            }

            // It's an absolute path to file
            if (Paths.get(hdtFileName).isAbsolute()) {
                return Paths.get(hdtFileName);
            }

            // It's relative to the 'facts' dir
            if (!hdtFileName.startsWith("http://")) {
                return Paths.get(dataDir, dbId, "facts", hdtFileName);
            }

            // copy remote to default file location
            download(hdtFileName, defaultPath.toFile());
            return defaultPath;
        }
        else {
            // default attempt when not explicit configured
            return defaultPath;
        }
    }

    private void download(String urlStr, File file) throws IOException {
        logger.info(marker, "Downloading from: '{}'", urlStr);
        URL url = new URL(urlStr);
        try (BufferedInputStream bis = new BufferedInputStream(url.openStream())) {
            try (FileOutputStream fis = new FileOutputStream(file)) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = bis.read(buffer, 0, 8192)) != -1) {
                    fis.write(buffer, 0, count);
                }
            }
        }
    }

}
