package net.stargraph.server;

/*-
 * ==========================License-Start=============================
 * stargraph-server
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

import com.google.common.base.Preconditions;
import net.stargraph.core.Stargraph;
import net.stargraph.core.index.Indexer;
import net.stargraph.data.Indexable;
import net.stargraph.model.Document;
import net.stargraph.model.KBId;
import net.stargraph.rest.KBResource;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

final class KBResourceImpl implements KBResource {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Marker marker = MarkerFactory.getMarker("server");

    private Stargraph core;

    KBResourceImpl(Stargraph core) {
        Preconditions.checkNotNull(core);
        this.core = core;
    }

    @Override
    public List<String> getKBs() {
        return core.getKBs()
                .stream()
                .map(kbId -> String.format("%s/%s", kbId.getId(), kbId.getType()))
                .sorted(String::compareTo)
                .collect(Collectors.toList());
    }

    @Override
    public Response load(String id, String type, boolean reset, int limit) {
        KBId kbId = KBId.of(id, type);
        Indexer indexer = core.getIndexer(kbId);
        indexer.load(reset, limit);
        return ResourceUtils.createAckResponse(true);
    }

    @Override
    public Response loadAll(String id, String resetKey) {
        core.getKBLoader(id).loadAll(resetKey);
        return ResourceUtils.createAckResponse(true);
    }

    @Override
    public Response upload(String id, String type, FormDataMultiPart form) {
        final KBId kbId = KBId.of(id, type);
        Indexer indexer = core.getIndexer(kbId);

        // get file information
        FormDataBodyPart filePart = form.getField("file");
        InputStream fileInputStream = filePart.getValueAs(InputStream.class);
        ContentDisposition contentDisposition =  filePart.getContentDisposition();
        String fileName = contentDisposition.getFileName();
        MediaType mediaType = filePart.getMediaType();
        String content;
        try {
            content = IOUtils.toString(fileInputStream, "UTF-8");
        } catch (IOException e) {
            logger.error(marker, "Failed to retrieve document content");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        // index
        try {
            if (type.equals("documents")) {
                String docId = fileName; //TODO get from other source?
                String docTitle = FilenameUtils.removeExtension(fileName); //TODO get from other source?
                indexer.index(new Indexable(new Document(docId, docTitle, null, content), kbId));
                indexer.flush();
            } else {
                logger.error(marker, "Type not supported yet: " + type);
                return Response.status(Response.Status.NOT_IMPLEMENTED).build();
            }
        } catch (InterruptedException e) {
            logger.error(marker, "Indexing failed.");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return ResourceUtils.createAckResponse(true);
    }
}
