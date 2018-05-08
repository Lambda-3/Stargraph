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
import net.stargraph.core.graph.BaseGraphModelProviderFactory;
import net.stargraph.core.graph.GraphModelProvider;
import net.stargraph.core.graph.JModel;
import net.stargraph.core.impl.hdt.HDTModelFileLoader;
import net.stargraph.core.impl.ntriples.NTriplesModelFileLoader;
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
    private class TestGraphModelProviderFactory extends BaseGraphModelProviderFactory {

        public TestGraphModelProviderFactory(Stargraph stargraph) {
            super(stargraph);
        }

        @Override
        public GraphModelProvider create(String dbId) {
            final KBId kbId = KBId.of(dbId, "facts");

            return new GraphModelProvider(
                    Arrays.asList(
                            new FileDataSource(stargraph, kbId, ClassLoader.getSystemResource("dataSets/obama/facts/triples.hdt").getPath(), "triples.hdt") {
                                @Override
                                protected Iterator getIterator(Stargraph stargraph, KBId kbId, File file) {
                                    return new HDTModelFileLoader(kbId.getId(), file, false).loadModelAsIterator();
                                }
                            }
                            ,
                            new FileDataSource(stargraph, kbId, ClassLoader.getSystemResource("dataSets/obama/facts/Michelle_Obama.nt").getPath(), "triples.nt") {
                                @Override
                                protected Iterator getIterator(Stargraph stargraph, KBId kbId, File file) {
                                    return new NTriplesModelFileLoader(kbId.getId(), file).loadModelAsIterator();
                                }
                            }
                    )
            );
        }
    }

    private String dbId = "mytest";
    private Stargraph stargraph;

    @BeforeClass
    public void beforeClass() throws IOException {
        Path root = java.nio.file.Files.createTempFile("stargraph-", "-dataDir");
        createPath(root, KBId.of(dbId, "facts"));

        ConfigFactory.invalidateCaches();
        Config config = ConfigFactory.load().getConfig("stargraph");
        stargraph = new Stargraph(config, false);
        stargraph.setDataRootDir(root.toFile());
        stargraph.setDefaultGraphModelProviderFactory(new TestGraphModelProviderFactory(stargraph));
        stargraph.initialize();
    }

    @Test
    public void loadJointGraphModel() {
        JModel model = stargraph.getKBCore(dbId).getGraphModel();

        System.out.println(model.size());
        Assert.assertTrue(model.size() > 2000);
    }
}
