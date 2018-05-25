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
import net.stargraph.ModelCreator;
import net.stargraph.ModelUtils;
import net.stargraph.core.processors.Processors;
import net.stargraph.core.processors.RegExFilterProcessor;
import net.stargraph.data.processor.Holder;
import net.stargraph.data.processor.Processor;
import net.stargraph.model.KBId;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class RegExFilterProcessorTest {

    KBId kbId = KBId.of("obama", "facts");

    @Test
    public void defaultFilterTest() {
        Config defaultCfg = ConfigFactory.load().getConfig("processor").withOnlyPath("regex-filter");
        System.out.println(ModelUtils.toStr(defaultCfg));
        Processor processor = Processors.create(defaultCfg);

        Holder fact1 = ModelCreator.createWrappedFact(kbId,
                "dbr:President_of_the_United_States", "rdfs:seeAlso", "dbr:Barack_Obama");

        processor.run(fact1);
        Assert.assertTrue(fact1.isSinkable());
    }

    @Test
    public void noFilterTest() {
        Config cfg = buildConfig(null, null, null);
        Processor processor = Processors.create(cfg);

        Holder fact1 = ModelCreator.createWrappedFact(kbId,
                "http://dbpedia.org/resource/President_of_the_United_States",
                "http://dbpedia.org/property/incumbent",
                "http://dbpedia.org/resource/Barack_Obama");

        Assert.assertFalse(fact1.isSinkable());

        processor.run(fact1);
        Assert.assertFalse(fact1.isSinkable());
    }

    @Test
    public void filterAllTest() {
        Holder fact1 = ModelCreator.createWrappedFact(kbId,
                "http://dbpedia.org/resource/President_of_the_United_States",
                "http://dbpedia.org/property/incumbent",
                "http://dbpedia.org/resource/Barack_Obama");

        Assert.assertFalse(fact1.isSinkable());

        Config cfg = buildConfig(".*", null, null);
        Processor processor = Processors.create(cfg);
        processor.run(fact1);
        Assert.assertTrue(fact1.isSinkable());

        fact1.setSink(false); //Should allow this?

        Config cfg2 = buildConfig(null, ".*", null);
        Processor processor2 = Processors.create(cfg2);
        processor2.run(fact1);
        Assert.assertTrue(fact1.isSinkable());

        fact1.setSink(false); //Should allow this?

        Config cfg3 = buildConfig(null, null, ".*");
        Processor processor3 = Processors.create(cfg3);
        processor3.run(fact1);
        Assert.assertTrue(fact1.isSinkable());
    }

    @Test
    public void filterTest() {
        Holder fact1 = ModelCreator.createWrappedFact(kbId,
                "http://dbpedia.org/resource/President_of_the_United_States",
                "http://dbpedia.org/property/incumbent",
                "http://dbpedia.org/resource/Barack_Obama");

        Assert.assertFalse(fact1.isSinkable());

        Config cfg = buildConfig(null, "^http://dbpedia.org/property/inc(.*)$", null);
        Processor processor = Processors.create(cfg);
        processor.run(fact1);
        Assert.assertTrue(fact1.isSinkable());
    }


    private Config buildConfig(String sRegex, String pRegex, String oRegex) {
        return ConfigFactory.parseMap(new HashMap() {{
            Map<String, Object> innerMap = new HashMap() {{
                put("s", sRegex != null ? Collections.singleton(sRegex) : null);
                put("p", pRegex != null ? Collections.singleton(pRegex) : null);
                put("o", oRegex != null ? Collections.singleton(oRegex) : null);
            }};
            put(RegExFilterProcessor.name, innerMap);
        }});
    }
}
