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
import net.stargraph.core.data.FactProviderFactory;
import net.stargraph.core.impl.ntriples.NTriplesModelProviderFactory;
import net.stargraph.core.Stargraph;
import net.stargraph.data.DataProvider;
import net.stargraph.model.KBId;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

public class FactProviderTest {
    private Config config;
    private Path root;

    @BeforeMethod
    public void before() throws IOException {
        root = TestUtils.prepareObamaTestEnv();
        ConfigFactory.invalidateCaches();
        config = ConfigFactory.load().getConfig("stargraph");
    }

    @Test
    public void factIterateTest() {
        Stargraph core = new Stargraph(config, false);
        core.setDataRootDir(root.toFile());
        core.initialize();
        KBId kbId = KBId.of("obama", "facts");
        FactProviderFactory factory = new FactProviderFactory(core);
        DataProvider<?> provider = factory.create(kbId);
        Assert.assertEquals(provider.getMergedDataSource().getStream().count(), 1877);
    }

    @Test
    public void factFromNTriplesTest() throws IOException {
        Stargraph core = new Stargraph(config, false);
        core.setDataRootDir(root.toFile());
        core.setDefaultGraphModelProviderFactory(new NTriplesModelProviderFactory(core));
        core.initialize();

        KBId kbId = KBId.of("obama", "facts");
        FactProviderFactory factory = new FactProviderFactory(core);
        DataProvider<?> provider = factory.create(kbId);
        Assert.assertEquals(provider.getMergedDataSource().getStream().count(), 1877);
    }

}
