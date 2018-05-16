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
import net.stargraph.core.data.FileDataSource;
import net.stargraph.core.graph.*;
import net.stargraph.core.impl.hdt.HDTModelFileLoader;
import net.stargraph.data.DataSource;
import net.stargraph.model.KBId;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;

import static net.stargraph.test.TestUtils.createPath;

public final class GraphModelProviderFactoryTest {
    private class DataSourceGenerator {
        private Stargraph stargraph;
        private KBId kbId;

        public DataSourceGenerator(Stargraph stargraph, KBId kbId) {
            this.stargraph = stargraph;
            this.kbId = kbId;
        }

        public DataSource getHDTDataSource() {
            return new FileDataSource(stargraph, kbId, ClassLoader.getSystemResource("dataSets/obama/facts/triples.hdt").getPath()) {
                @Override
                protected Iterator createIterator(Stargraph stargraph, KBId kbId, File file) {
                    return new HDTModelFileLoader(kbId.getId(), file, false).loadModelAsIterator();
                }
            };
        }

        public DataSource getNTDataSource() {
            return new FileDataSource(stargraph, kbId, ClassLoader.getSystemResource("dataSets/obama/facts/triples.nt").getPath()) {
                @Override
                protected Iterator createIterator(Stargraph stargraph, KBId kbId, File file) {
                    return new DefaultModelFileLoader(kbId.getId(), file).loadModelAsIterator();
                }
            };
        }

        public DataSource getTurtleDataSource() {
            return new FileDataSource(stargraph, kbId, ClassLoader.getSystemResource("dataSets/obama/facts/triples.ttl").getPath()) {
                @Override
                protected Iterator createIterator(Stargraph stargraph, KBId kbId, File file) {
                    return new DefaultModelFileLoader(kbId.getId(), file).loadModelAsIterator();
                }
            };
        }

        public DataSource getNewDataSource() {
            return new FileDataSource(stargraph, kbId, ClassLoader.getSystemResource("dataSets/obama/facts/Michelle_Obama.nt").getPath()) {
                @Override
                protected Iterator createIterator(Stargraph stargraph, KBId kbId, File file) {
                    return new DefaultModelFileLoader(kbId.getId(), file).loadModelAsIterator();
                }
            };
        }
    }

    private interface FactoryGenerator {
        GraphModelProviderFactory generate(Stargraph stargraph, KBId kbId);
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
        stargraph.setDefaultGraphModelProviderFactory(factoryGenerator.generate(stargraph, kbId));
        stargraph.initialize();

        JModel model = stargraph.getKBCore(kbId.getId()).getGraphModel();
        Assert.assertTrue(model.size() > 2000);
    }

    @Test
    public void loadJointGraphModelHDTNEWTest() throws Exception {

        loadJointGraphModelTest(new FactoryGenerator() {
            @Override
            public GraphModelProviderFactory generate(Stargraph stargraph, KBId kbId) {
                DataSourceGenerator generator = new DataSourceGenerator(stargraph, kbId);
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
            public GraphModelProviderFactory generate(Stargraph stargraph, KBId kbId) {
                DataSourceGenerator generator = new DataSourceGenerator(stargraph, kbId);
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
            public GraphModelProviderFactory generate(Stargraph stargraph, KBId kbId) {
                DataSourceGenerator generator = new DataSourceGenerator(stargraph, kbId);
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
