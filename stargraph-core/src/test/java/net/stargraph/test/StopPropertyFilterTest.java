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
import net.stargraph.ModelUtils;
import net.stargraph.core.processors.Processors;
import net.stargraph.core.processors.StopPropertyFilterProcessor;
import net.stargraph.data.processor.Holder;
import net.stargraph.data.processor.Processor;
import net.stargraph.model.KBId;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unchecked")
public class StopPropertyFilterTest {
    private KBId kbId = KBId.of("any", "thing");

    @Test
    public void simpleTest() {
        Config cfg = buildConfig(Collections.singletonList("^rdf:type$"));

        Holder holder = ModelUtils.createWrappedProperty(kbId, "rdf:type");
        Assert.assertFalse(holder.isSinkable());

        Processor processor = Processors.create(cfg);
        processor.run(holder);

        Assert.assertTrue(holder.isSinkable());
    }

    @Test
    public void noFilterTest() {
        Config cfg = buildConfig(Collections.emptyList());

        Holder holder = ModelUtils.createWrappedProperty(kbId, "rdf:type");
        Assert.assertFalse(holder.isSinkable());

        Processor processor = Processors.create(cfg);
        processor.run(holder);

        Assert.assertFalse(holder.isSinkable());
    }

    @Test
    public void noMatchTest() {
        Config cfg = buildConfig(Collections.emptyList());

        Holder holder = ModelUtils.createWrappedProperty(kbId, "rdf:x");
        Assert.assertFalse(holder.isSinkable());

        Processor processor = Processors.create(cfg);
        processor.run(holder);

        Assert.assertFalse(holder.isSinkable());
    }

    private Config buildConfig(List<String> strPatterns) {
        return ConfigFactory.parseMap(new HashMap() {{
            put(StopPropertyFilterProcessor.name, strPatterns);
        }});
    }
}
