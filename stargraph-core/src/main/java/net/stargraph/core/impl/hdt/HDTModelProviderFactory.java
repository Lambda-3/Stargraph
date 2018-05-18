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

        final String cfgUseIndexPath = "graphmodel.hdt.use-index";
        boolean useIndex = config.hasPath(cfgUseIndexPath) && config.getBoolean(cfgUseIndexPath);

        final String cfgInMemoryPath = "graphmodel.hdt.in-memory";
        boolean inMemory = config.hasPath(cfgInMemoryPath)? config.getBoolean(cfgInMemoryPath) : true;

        final String cfgResetPath = "graphmodel.hdt.reset";
        boolean reset = config.hasPath(cfgResetPath) && config.getBoolean(cfgResetPath);

        return new GraphModelProvider(
                stargraph, dbId, inMemory, reset, new HDTFileGraphSource(stargraph, dbId, resourcePath, null, true, useIndex)
        );
    }
}
