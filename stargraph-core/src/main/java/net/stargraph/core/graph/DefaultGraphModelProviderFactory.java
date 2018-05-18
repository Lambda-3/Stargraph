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

import com.typesafe.config.Config;
import net.stargraph.core.Stargraph;
import net.stargraph.core.impl.hdt.HDTFileGraphSource;
import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.List;

public class DefaultGraphModelProviderFactory extends BaseGraphModelProviderFactory {

    public DefaultGraphModelProviderFactory(Stargraph stargraph) {
        super(stargraph);
    }

    @Override
    public GraphModelProvider create(String dbId) {
        Config providerCfg = stargraph.getGraphModelProviderCfg(dbId);

        final String sourcesCfgPath = "resources";
        List<String> resources = new ArrayList<>();
        if (providerCfg.hasPath(sourcesCfgPath)) {
            resources = providerCfg.getStringList(sourcesCfgPath);
        }

        Config graphModelCfg = stargraph.getGraphModelConfig(dbId);

        final String inMemoryCfgPath = "in-memory";
        boolean inMemory = graphModelCfg.hasPath(inMemoryCfgPath)? graphModelCfg.getBoolean(inMemoryCfgPath) : true;

        final String resetCfgPath = "reset";
        boolean reset = graphModelCfg.hasPath(resetCfgPath) && graphModelCfg.getBoolean(resetCfgPath);

        final String hdtUseIndexCfgPath = "hdt.use-index";
        boolean hdtUseIndex = graphModelCfg.hasPath(hdtUseIndexCfgPath) && graphModelCfg.getBoolean(hdtUseIndexCfgPath);


        // create graph sources
        List<GraphSource<BaseGraphModel>> graphSources = new ArrayList<>();
        for (String resource : resources) {
            String extension = FilenameUtils.getExtension(resource).toLowerCase();

            if (extension.equals("hdt")) {
                graphSources.add(new HDTFileGraphSource(stargraph, dbId, resource, null, true, hdtUseIndex));
            } else {
                graphSources.add(new DefaultFileGraphSource(stargraph, dbId, resource, null, true));
            }
        }

        return new GraphModelProvider(stargraph, dbId, inMemory, reset, graphSources);
    }
}
