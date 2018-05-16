package net.stargraph.core.impl.hdt;

import com.typesafe.config.Config;
import net.stargraph.core.Stargraph;
import net.stargraph.core.data.FileDataSource;
import net.stargraph.core.graph.BaseGraphModelProviderFactory;
import net.stargraph.core.graph.GraphModelProvider;
import net.stargraph.model.KBId;

import java.io.File;
import java.util.Iterator;

public class HDTModelProviderFactory extends BaseGraphModelProviderFactory {

    public HDTModelProviderFactory(Stargraph stargraph) {
        super(stargraph);
    }

    @Override
    public GraphModelProvider create(String dbId) {
        final KBId kbId = KBId.of(dbId, "facts");
        Config config = stargraph.getKBCore(dbId).getConfig();

        final String cfgFilePath = "graphmodel.hdt.file";
        String resourcePath = "triples.hdt";
        if (config.hasPath(cfgFilePath)) {
            resourcePath = config.getString(cfgFilePath);
        }

        boolean useIndex = config.hasPath("graphmodel.hdt.use-index") && config.getBoolean("graphmodel.hdt.use-index");

        return new GraphModelProvider(
                new FileDataSource(stargraph, kbId, resourcePath) {
                    @Override
                    protected Iterator createIterator(Stargraph stargraph, KBId kbId, File file) {
                        return new HDTModelFileLoader(kbId.getId(), file, useIndex).loadModelAsIterator();
                    }
                }
        );
    }
}
