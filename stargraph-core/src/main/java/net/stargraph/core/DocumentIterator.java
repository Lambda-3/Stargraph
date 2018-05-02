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
import net.stargraph.StarGraphException;
import net.stargraph.core.serializer.ObjectSerializer;
import net.stargraph.data.Indexable;
import net.stargraph.model.Document;
import net.stargraph.model.KBId;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Objects;

public final class DocumentIterator implements Iterator<Indexable> {
    private static Logger logger = LoggerFactory.getLogger(DocumentIterator.class);
    private static Marker marker = MarkerFactory.getMarker("core");

    private Stargraph core;
    private final KBId kbId;
    private final ObjectMapper mapper;
    private final Iterator<String> lineIt;
    private Document next;


    public DocumentIterator(Stargraph core, KBId kbId) {
        this.core = Objects.requireNonNull(core);
        this.kbId = Objects.requireNonNull(kbId);
        this.mapper = ObjectSerializer.createMapper(kbId);

        Path filePath = getFilePath(kbId.getId());
        File file = filePath.toFile();
        try {
            this.lineIt = FileUtils.lineIterator(file, "UTF-8");
            parseNext();
        } catch (IOException e) {
            logger.error(marker, "Failed to load documents from file {}.", file);
            throw new StarGraphException(e);
        }
    }

    private void parseNext() {
        while (lineIt.hasNext()) {
            String line = lineIt.next();
            if (line.length() > 0) {
                try {
                    Document document = mapper.readValue(line, Document.class);
                    next = document;
                    return;
                } catch (IOException e) {
                    logger.warn(marker, "Failed to deserialize document from line: {}", line);
                }
            }
        }
        next = null;
    }

    private Path getFilePath(String  dbId) {
        String dataDir = core.getDataRootDir();
        return Paths.get(dataDir, dbId, "documents", "documents.json");
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public Indexable next() {
        Indexable indexable = new Indexable(next, kbId);
        parseNext();
        return indexable;
    }

}
