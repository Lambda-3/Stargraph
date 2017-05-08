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

import com.typesafe.config.Config;
import net.stargraph.core.Stargraph;
import net.stargraph.core.Version;
import net.stargraph.model.KBId;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequestBuilder;
import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeRequestBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.ClearScrollRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.net.InetAddress;
import java.util.Collection;
import java.util.List;

public final class ElasticClient {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Marker marker = MarkerFactory.getMarker("elastic");
    private KBId kbId;
    private Client client;
    private Stargraph core;
    private String indexName;

    public ElasticClient(Stargraph core, KBId kbId) {
        logger.trace(marker, "Creating ES Client for {}", kbId);
        this.core = core;
        this.kbId = kbId;
        this.client = createClient();
        this.indexName = createIndexName();
    }

    public QueryBuilder buildIdsQuery(Collection<String> ids) {
        IdsQueryBuilder queryBuilder = QueryBuilders.idsQuery(getIndexType());
        queryBuilder.ids().addAll(ids);
        return queryBuilder;
    }

    public MoreLikeThisQueryBuilder.Item buildMLTItem(String id) {
        return new MoreLikeThisQueryBuilder.Item(getIndexName(), getIndexType(), id);
    }

    public SearchRequestBuilder prepareSearch() {
        return client.prepareSearch(getIndexName()).setTypes(getIndexType()).setSize(10000);
    }

    public SearchScrollRequestBuilder prepareSearchScroll(String scrollId) {
        return client.prepareSearchScroll(scrollId);
    }

    public ClearScrollRequestBuilder prepareClearScroll(String scrollId) {
        return client.prepareClearScroll().addScrollId(scrollId);
    }

    public ForceMergeRequestBuilder prepareForceMerge() {
        return client.admin().indices().prepareForceMerge(getIndexName()).setFlush(true);
    }

    public IndexRequest createIndexRequest(String id, boolean create) {
        return new IndexRequest(getIndexName(), getIndexType(), id).create(create);
    }

    public UpdateRequest createUpdateRequest(String id) {
        return new UpdateRequest(getIndexName(), getIndexType(), id);
    }

    DeleteIndexRequestBuilder prepareDelete() {
        logger.info(marker, "Deleting {}", kbId);
        return client.admin().indices().prepareDelete(getIndexName());
    }

    IndicesExistsRequestBuilder prepareExists() {
        return client.admin().indices().prepareExists(getIndexName());
    }

    CreateIndexRequestBuilder prepareCreate() {
        logger.info(marker, "Creating {}", kbId);
        Config mappingCfg = getTypeCfg().getConfig("elastic.mapping");
        // Search for matching mapping definition, fallback to the dynamic _default_.
        String targetType = mappingCfg.hasPath(kbId.getType()) ? kbId.getType() : "_default_";
        Config mapping = mappingCfg.withOnlyPath(targetType);
        CreateIndexRequestBuilder builder = client.admin().indices().prepareCreate(getIndexName());
        return builder.addMapping(targetType, mapping.root().unwrapped());
    }

    Client getTransport() {
        return client;
    }

    @Override
    public String toString() {
        return "ElasticClient{'" + kbId + "'}";
    }

    private String getIndexName() {
        return indexName;
    }

    private String getIndexType() {
        return kbId.getType();
    }

    private Config getTypeCfg() {
        return core.getTypeConfig(kbId);
    }

    private TransportClient createClient() {
        Config cfg = getTypeCfg();
        Settings settings = Settings.builder().put("cluster.name", cfg.getString("elastic.cluster-name")).build();
        TransportClient client = new PreBuiltTransportClient(settings);

        List<String> servers = cfg.getStringList("elastic.servers");
        logger.debug(marker, "Elastic Servers: {}", servers);
        for (String addr : servers) {
            try {
                String[] a = addr.split(":");
                String host = a[0];
                int port = Integer.parseInt(a[1]);
                client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
            } catch (Exception e) {
                logger.error(marker, "Transport client creation failed for '{}'", addr, e);
            }
        }

        return client;
    }

    private String createIndexName() {
        String cfgPath = "elastic.index.prefix-name";
        String codeName = Version.getCodeName().replace(" ", "-").toLowerCase();
        String prefix = core.getConfig().getIsNull(cfgPath) ? codeName : core.getConfig().getString(cfgPath);
        return String.format("%s.%s.%s", prefix, kbId.getId(), kbId.getType());
    }
}
