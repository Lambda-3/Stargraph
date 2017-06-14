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
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import net.stargraph.StarGraphException;
import net.stargraph.core.DocumentProviderFactory;
import net.stargraph.core.Stargraph;
import net.stargraph.core.index.Indexer;
import net.stargraph.model.Document;
import net.stargraph.model.KBId;
import net.stargraph.rest.KBResource;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    private String determineType(MediaType mediaType) {
        Config config = core.getConfig();
        String cfgPath = "media-type-mappings" + "." + mediaType.toString();

        if (config.hasPath(cfgPath)) {
            return config.getString(cfgPath);
        }

        return null;
    }

    @Override
    public Response upload(String id, FormDataMultiPart form) {

        // get file information
        FormDataBodyPart filePart = form.getField("file");
        InputStream fileInputStream = filePart.getValueAs(InputStream.class);
        ContentDisposition contentDisposition =  filePart.getContentDisposition();
        String fileName = contentDisposition.getFileName();
        MediaType mediaType = filePart.getMediaType();

        // determine type by mediaType
        String type = determineType(mediaType);
        if (type == null) {
            return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
        }

        // only supported for document type yet
        if (type.equals("documents")) {
            String title = FilenameUtils.removeExtension(fileName);
            try {
                String content = IOUtils.toString(fileInputStream, "UTF-8");
                Document document = new Document(title, content);

                DocumentProviderFactory.storeDocument(KBId.of(id, type), document);
            } catch (IOException e) {
                logger.error(marker, "Failed to retrieve document content");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            //TODO implement for other types
            logger.error(marker, "Not implemented");
            return Response.status(Response.Status.NOT_IMPLEMENTED).build();
        }

        return ResourceUtils.createAckResponse(true);
    }

    @Override
    public Response clear(String id, String type) {

        // only supported for document type yet
        if (type.equals("documents")) {
            DocumentProviderFactory.clearDocuments(KBId.of(id, type));
        } else {
            //TODO implement for other types
            logger.error(marker, "Not implemented");
            return Response.status(Response.Status.NOT_IMPLEMENTED).build();
        }

        return ResourceUtils.createAckResponse(true);
    }


}
