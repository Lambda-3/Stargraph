package net.stargraph.test;

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
import com.typesafe.config.ConfigFactory;
import net.stargraph.core.Stargraph;
import net.stargraph.core.graph.*;
import net.stargraph.core.impl.hdt.HDTFileGraphSource;
import net.stargraph.model.KBId;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import static net.stargraph.test.TestUtils.createPath;

public final class GraphModelProviderFactoryTest {

    private class GraphSourceGenerator {
        private Stargraph stargraph;
        private String dbId;

        public GraphSourceGenerator(Stargraph stargraph, String dbId) {
            this.stargraph = stargraph;
            this.dbId = dbId;
        }

        public GraphSource getHDTDataSource() {
            return new HDTFileGraphSource(stargraph, dbId, ClassLoader.getSystemResource("dataSets/obama/facts/triples.hdt").getPath(), null, true, false);
        }

        public GraphSource getNTDataSource() {
            return new DefaultFileGraphSource(stargraph, dbId, ClassLoader.getSystemResource("dataSets/obama/facts/triples.nt").getPath(), null, true);
        }

        public GraphSource getTurtleDataSource() {
            return new DefaultFileGraphSource(stargraph, dbId, ClassLoader.getSystemResource("dataSets/obama/facts/triples.ttl").getPath(), null, true);
        }

        public GraphSource getNewDataSource() {
            return new DefaultFileGraphSource(stargraph, dbId, ClassLoader.getSystemResource("dataSets/obama/facts/Michelle_Obama.nt").getPath(), null, true);
        }
    }

    private interface FactoryGenerator {
        GraphModelProviderFactory generate(Stargraph stargraph, String dbId);
    }


    private KBId kbId = KBId.of("mytest", "facts");
    private Stargraph stargraph;

    private void loadJointGraphModelTest(FactoryGenerator factoryGenerator) throws IOException {
        Path root = java.nio.file.Files.createTempFile("stargraph-", "-dataDir");
        createPath(root, kbId);

        ConfigFactory.invalidateCaches();
        Config config = ConfigFactory.load().getConfig("stargraph");
        stargraph = new Stargraph(config, false);
        stargraph.setDataRootDir(root.toFile());
        stargraph.setDefaultGraphModelProviderFactory(factoryGenerator.generate(stargraph, kbId.getId()));
        stargraph.initialize();

        BaseGraphModel model = stargraph.getKBCore(kbId.getId()).getGraphModel();
        Assert.assertTrue(model.getSize() > 2000);
    }

    @Test
    public void loadJointGraphModelHDTNEWTest() throws Exception {

        loadJointGraphModelTest(new FactoryGenerator() {
            @Override
            public GraphModelProviderFactory generate(Stargraph stargraph, String dbId) {
                GraphSourceGenerator generator = new GraphSourceGenerator(stargraph, dbId);
                return new BaseGraphModelProviderFactory(stargraph) {
                    @Override
                    public GraphModelProvider create(String dbId) {
                        return new GraphModelProvider(Arrays.asList(
                                generator.getHDTDataSource(),
                                generator.getNewDataSource()
                        ));
                    }
                };
            }
        });
    }

    @Test
    public void loadJointGraphModelNEWHDTTest() throws Exception {

        loadJointGraphModelTest(new FactoryGenerator() {
            @Override
            public GraphModelProviderFactory generate(Stargraph stargraph, String dbId) {
                GraphSourceGenerator generator = new GraphSourceGenerator(stargraph, dbId);
                return new BaseGraphModelProviderFactory(stargraph) {
                    @Override
                    public GraphModelProvider create(String dbId) {
                        return new GraphModelProvider(Arrays.asList(
                                generator.getNewDataSource(),
                                generator.getHDTDataSource()
                                ));
                    }
                };
            }
        });
    }

    @Test
    public void loadJointGraphModelAllTest() throws Exception {

        loadJointGraphModelTest(new FactoryGenerator() {
            @Override
            public GraphModelProviderFactory generate(Stargraph stargraph, String dbId) {
                GraphSourceGenerator generator = new GraphSourceGenerator(stargraph, dbId);
                return new BaseGraphModelProviderFactory(stargraph) {
                    @Override
                    public GraphModelProvider create(String dbId) {
                        return new GraphModelProvider(Arrays.asList(
                                generator.getNTDataSource(),
                                generator.getHDTDataSource(),
                                generator.getTurtleDataSource(),
                                generator.getNewDataSource()
                        ));
                    }
                };
            }
        });
    }
}
