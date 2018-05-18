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

import net.stargraph.StarGraphException;
import net.stargraph.core.Stargraph;
import net.stargraph.core.data.DataUtils;
import net.stargraph.model.KBId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;

public abstract class FileGraphSource implements GraphSource<BaseGraphModel> {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected Marker marker = MarkerFactory.getMarker("graph");

    protected final Stargraph stargraph;
    protected final String dbId;
    private final String storeFilename; // optional
    private final String resource;
    private final boolean required;

    public FileGraphSource(Stargraph stargraph, String dbId, String resource, String storeFilename, boolean required) {
        this.stargraph = Objects.requireNonNull(stargraph);
        this.dbId = Objects.requireNonNull(dbId);
        this.resource = Objects.requireNonNull(resource);
        this.storeFilename = storeFilename;
        this.required = required;
    }

    protected abstract void extend(BaseGraphModel graphModel, File file);

    @Override
    public void extend(BaseGraphModel graphModel) {
        try {
            File file = DataUtils.getData(stargraph, resource, KBId.of(dbId, "facts"), storeFilename).toFile();
            if (!file.exists()) {
                if (required) {
                    throw new FileNotFoundException("File not found: '" + file + "'");
                } else {
                    logger.info("File not found: '" + file + "'");
                    return;
                }
            } else {
                extend(graphModel, file);
            }
        } catch (Exception e) {
            throw new StarGraphException(e);
        }
    }
}
