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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.stargraph.StarGraphException;
import net.stargraph.core.serializer.ObjectSerializer;
import net.stargraph.data.DataProvider;
import net.stargraph.data.Indexable;
import net.stargraph.model.Document;
import net.stargraph.model.KBId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Encapsulates the logic to provide a stream of documents.
 */
public final class DocumentProviderFactory extends BaseDataProviderFactory {
    private static final Logger logger = LoggerFactory.getLogger(DocumentProviderFactory.class);
    protected static Marker marker = MarkerFactory.getMarker("core");

    private static Config config = ConfigFactory.load().getConfig("stargraph");


    public DocumentProviderFactory(Stargraph core) {
        super(core);
    }

    private static Path getDataFilePath(String  dbId) {
        String dataDir = config.getString("data.root-dir");
        Path defaultPath = Paths.get(dataDir, dbId, "documents", "documents.data");

        return defaultPath;
    }

    public static void clearDocuments(KBId kbId) {
        Path dataFilePath = getDataFilePath(kbId.getId());
        File file = dataFilePath.toFile();
        if (file.exists()) {
            if (!file.delete()) {
                logger.error(marker, "Could not delete documents.");
            } else {
                logger.info(marker, "Deleted documents.");
            }
        }
    }

    public static void storeDocument(KBId kbId, Document document) {
        Path dataFilePath = getDataFilePath(kbId.getId());

        List<Document> documents = new ArrayList<>();
        documents.addAll(loadDocuments(kbId));
        documents.add(document);

        try {
            File file = dataFilePath.toFile();
            Files.createDirectories(Objects.requireNonNull(file).toPath().getParent());

            ObjectMapper mapper = ObjectSerializer.createMapper(kbId);
            mapper.writeValue(file, documents);

            logger.info(marker, "Stored document {}", document.toString());
        } catch (IOException e) {
            logger.error(marker, "Failed to store document {}", document.toString());
            throw new StarGraphException(e);
        }
    }

    private static List<Document> loadDocuments(KBId kbId) {
        Path dataFilePath = getDataFilePath(kbId.getId());

        File file = dataFilePath.toFile();
        if (!file.exists()) {
            logger.warn(marker, "Did not find any documents.");
            return Collections.unmodifiableList(Collections.emptyList());
        }

        try {
            ObjectMapper mapper = ObjectSerializer.createMapper(kbId);
            List<Document> documents = mapper.readValue(file, new TypeReference<List<Document>>() {});

            logger.info(marker, "loaded documents:");
            documents.forEach(d -> logger.info(marker, d.toString()));

            return documents;
        } catch (IOException e) {
            logger.error(marker, "Failed to load documents.");
            throw new StarGraphException(e);
        }
    }

    @Override
    public DataProvider<Indexable> create(KBId kbId) {

        List<Document> documents = loadDocuments(kbId);

        return new DataProvider<>(new DocumentIterator(kbId, documents));
    }

}
