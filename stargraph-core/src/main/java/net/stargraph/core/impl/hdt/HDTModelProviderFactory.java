package net.stargraph.core.impl.hdt;

import com.typesafe.config.Config;
import net.stargraph.core.Stargraph;
import net.stargraph.core.graph.BaseGraphModelProviderFactory;
import net.stargraph.core.graph.GraphModelProvider;

public class HDTModelProviderFactory extends BaseGraphModelProviderFactory {

    public HDTModelProviderFactory(Stargraph stargraph) {
        super(stargraph);
    }

    @Override
    public GraphModelProvider create(String dbId) {
        Config config = stargraph.getKBCore(dbId).getConfig();

        final String cfgFilePath = "graphmodel.hdt.file";
        String resourcePath = "triples.hdt";
        if (config.hasPath(cfgFilePath)) {
            resourcePath = config.getString(cfgFilePath);
        }

        boolean useIndex = config.hasPath("graphmodel.hdt.use-index") && config.getBoolean("graphmodel.hdt.use-index");

        return new GraphModelProvider(
                new HDTFileGraphSource(stargraph, dbId, resourcePath, null, true, useIndex)
        );
    }
}
