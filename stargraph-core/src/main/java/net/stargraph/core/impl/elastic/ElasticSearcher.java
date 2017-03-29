package net.stargraph.core.impl.elastic;

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
import net.stargraph.core.Stargraph;
import net.stargraph.core.search.BaseSearcher;
import net.stargraph.core.search.SearchQueryHolder;
import net.stargraph.core.serializer.ObjectSerializer;
import net.stargraph.model.KBId;
import net.stargraph.rank.Score;
import net.stargraph.rank.Scores;
import org.elasticsearch.search.SearchHit;

import java.io.Serializable;

public final class ElasticSearcher extends BaseSearcher {
    private ObjectMapper mapper;
    private ElasticClient esClient;

    public ElasticSearcher(KBId kbId, Stargraph core) {
        super(kbId, core);
        this.mapper = ObjectSerializer.createMapper(kbId);
    }

    @Override
    protected void onStart() {
        this.esClient = new ElasticClient(core, this.kbId);
    }

    @Override
    protected void onStop() {
        if (this.esClient != null) {
            this.esClient.getTransport().close();
        }
    }

    @Override
    public Scores search(SearchQueryHolder holder) {
        String modelName = holder.getSearchParams().getKbId().getType();
        Class<Serializable> modelClass = core.getModelClass(modelName);

        ElasticScroller scroller = new ElasticScroller(esClient, holder) {
            @Override
            protected Score build(SearchHit hit) {
                try {
                    Serializable entity = mapper.readValue(hit.source(), modelClass);
                    return new Score(entity, hit.getScore());
                } catch (Exception e) {
                    logger.error(marker, "Fail to deserialize {}", hit.sourceAsString(), e);
                }
                return null;
            }
        };

        return scroller.getScores();
    }
}
