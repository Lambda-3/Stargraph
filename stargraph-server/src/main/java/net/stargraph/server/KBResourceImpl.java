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
import net.stargraph.model.KBId;
import net.stargraph.rest.KBResource;

import javax.ws.rs.core.Response;
import java.util.Set;
import java.util.stream.Collectors;

final class KBResourceImpl implements KBResource {

    private Stargraph core;

    KBResourceImpl(Stargraph core) {
        Preconditions.checkNotNull(core);
        this.core = core;
    }

    @Override
    public Set<String> getKBs() {
        return core.getKBs()
                .stream()
                .map(kbId -> String.format("%s/%s", kbId.getId(), kbId.getType()))
                .collect(Collectors.toSet());
    }

    @Override
    public Response load(String id, String type, boolean reset, int limit) {
        KBId kbId = KBId.of(id, type);
        Indexer indexer = core.getIndexer(kbId);
        indexer.load(reset, limit);
        return ResourceUtils.createAckResponse(true);
    }
}
