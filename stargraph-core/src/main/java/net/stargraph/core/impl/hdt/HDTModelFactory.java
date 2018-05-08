package net.stargraph.core.impl.hdt;

import com.typesafe.config.Config;
import net.stargraph.core.Stargraph;
import net.stargraph.core.data.FileDataSource;
import net.stargraph.core.graph.BaseGraphModelProviderFactory;
import net.stargraph.core.graph.GraphModelProvider;
import net.stargraph.model.KBId;

import java.io.File;
import java.util.Iterator;

public class HDTModelFactory extends BaseGraphModelProviderFactory {

    public HDTModelFactory(Stargraph stargraph) {
        super(stargraph);
    }

    @Override
    public GraphModelProvider create(String dbId) {
        final KBId kbId = KBId.of(dbId, "facts");
        String dataDir = stargraph.getDataRootDir();
        Config config = stargraph.getKBCore(dbId).getConfig();

        final String cfgFilePath = "graphmodel.hdt.file";
        String hdtResourcePath = "triples.hdt";
        if (config.hasPath(cfgFilePath)) {
            hdtResourcePath = config.getString(cfgFilePath);
        }

        Config tripleStoreCfg = stargraph.getKBCore(dbId).getConfig("graphmodel");
        boolean useIndex = tripleStoreCfg.hasPath("hdt.use-index") && tripleStoreCfg.getBoolean("hdt.use-index");

        return new GraphModelProvider(
                new FileDataSource(stargraph, kbId, hdtResourcePath, "triples.hdt") {
                    @Override
                    protected Iterator getIterator(Stargraph stargraph, KBId kbId, File file) {
                        return new HDTModelFileLoader(kbId.getId(), file, useIndex).loadModelAsIterator();
                    }
                }
        );
    }
}
