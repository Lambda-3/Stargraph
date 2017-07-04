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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.stargraph.StarGraphException;
import net.stargraph.core.serializer.ObjectSerializer;
import net.stargraph.data.Indexable;
import net.stargraph.model.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

final class DocumentIterator implements Iterator<Indexable> {
    private static Logger logger = LoggerFactory.getLogger(DocumentIterator.class);
    private static Marker marker = MarkerFactory.getMarker("core");

    private static Config config = ConfigFactory.load().getConfig("stargraph");

    private KBId kbId;
    private Iterator<File> fileIt;

    DocumentIterator(KBId kbId) {
        this.kbId = kbId;

        Path dataDirectory = getDataDirectory(kbId.getId());
        File dir = dataDirectory.toFile();
        if (!dir.exists()) {
            logger.warn(marker, "Did not find any documents.");
            fileIt = Collections.emptyIterator();
        } else {
            fileIt = FileUtils.iterateFiles(dir, new String[]{"json"}, false);
        }
    }

    private static Path getDataDirectory(String  dbId) {
        String dataDir = config.getString("data.root-dir");
        Path dataDirectory = Paths.get(dataDir, dbId, "documents");

        return dataDirectory;
    }

    public static void clearDocuments(KBId kbId) {
        Path dataDirectory = getDataDirectory(kbId.getId());
        File dir = dataDirectory.toFile();
        if (dir.exists() && dir.isDirectory()) {

            // delete files
            int n = 0;
            for(File file: dir.listFiles()) {
                if (!file.isDirectory()) {
                    if (!file.delete()) {
                        logger.error(marker, "Could not delete file {}.", file);
                    } else {
                        ++n;
                    }
                }
            }

            logger.info(marker, "Deleted {} documents", n);
        }
    }

    public static void storeDocument(KBId kbId, Document document) {
        Path dataDirectory = getDataDirectory(kbId.getId());
        File dir = dataDirectory.toFile();

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        String fileName = document.getTitle() + "-" + timeStamp + ".json";
        File file = new File(dir, fileName);

        ObjectMapper mapper = ObjectSerializer.createMapper(kbId);
        try {
            Files.createDirectories(Objects.requireNonNull(file).toPath().getParent());
            mapper.writeValue(file, document);
            logger.info(marker, "Stored document {}", document.toString());
        } catch (IOException e) {
            logger.error(marker, "Failed to store document {}", document.toString());
            throw new StarGraphException(e);
        }
    }

    @Override
    public boolean hasNext() {
        return fileIt.hasNext();
    }

    @Override
    public Indexable next() {
        ObjectMapper mapper = ObjectSerializer.createMapper(kbId);

        File file = fileIt.next();
        try {
            Document document = mapper.readValue(file, Document.class);

            return new Indexable(document, kbId);
        } catch (IOException e) {
            logger.error(marker, "Failed to load document from file {}.", file);
            throw new StarGraphException(e);
        }
    }
}
