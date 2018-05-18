package net.stargraph.core.data;

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

import net.stargraph.core.Stargraph;
import net.stargraph.model.KBId;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class DataUtils {
    private static Logger logger = LoggerFactory.getLogger(DataUtils.class);
    private static Marker marker = MarkerFactory.getMarker("core");

    public static Path getData(Stargraph stargraph, String resource, KBId kbId, String storeFilename) throws IOException {
        Path baseDir = Paths.get(stargraph.getDataRootDir(), kbId.getId(), kbId.getModel());

        return getData(resource, baseDir, storeFilename);
    }

    public static Path getData(String resource, Path baseDir, String storeFilename) throws IOException {

        // web resource
        if (resource.startsWith("http://")) {
            String stFilename = (storeFilename != null)? storeFilename : FilenameUtils.getName(resource);
            Path storePath = Paths.get(baseDir.toString(), stFilename);

            if (storePath.toFile().exists()) {
                // already downloaded
                return storePath;
            } else {
                // download
                download(resource, storePath.toFile());
                return storePath;
            }
        } else
            // It's an absolute path to file
            if (Paths.get(resource).isAbsolute()) {
                return Paths.get(resource);
            }
            else
            // It's relative to the storeDirectory
            {
                return Paths.get(baseDir.toString(), resource);
            }
    }

    private static void download(String urlStr, File file) throws IOException {
        logger.info(marker, "Downloading from: '{}'", urlStr);

        Files.createDirectories(Objects.requireNonNull(file).toPath().getParent());
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
