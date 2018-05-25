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
import net.stargraph.core.processors.CoreferenceResolutionProcessor;
import net.stargraph.core.processors.FactClassifierProcessor;
import net.stargraph.core.processors.Processors;
import net.stargraph.data.Indexable;
import net.stargraph.data.processor.FatalProcessorException;
import net.stargraph.data.processor.Holder;
import net.stargraph.data.processor.Processor;
import net.stargraph.data.processor.ProcessorChain;
import net.stargraph.model.*;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

@SuppressWarnings("unchecked")
public final class ProcessorsTest {

    private Config config;

    @BeforeClass
    public void beforeClass() {
        ConfigFactory.invalidateCaches();
        config = ConfigFactory.load().getConfig("processor");
    }

    @Test
    public void simpleProcessorChainTest() {
        final KBId kbId = KBId.of("obama", "facts");

        Holder holder = ModelUtils.createWrappedFact(kbId,
                "http://dbpedia.org/resource/FC_Oberlausitz_Neugersdorf",
                "http://purl.org/dc/terms/subject",
                "http://dbpedia.org/resource/Category:Football_clubs_in_Germany");

        Processor entityClassifierProcessor = Processors.create(config.withOnlyPath("fact-classifier"));
        Processor nsProcessor = Processors.create(config.withOnlyPath("sink-duplicate"));
        ProcessorChain chain = new ProcessorChain(Arrays.asList(entityClassifierProcessor, nsProcessor));


        chain.run(holder);
        Fact processed = (Fact) holder.get();

        Assert.assertEquals(((ResourceEntity) processed.getSubject()).getId(),
                "http://dbpedia.org/resource/FC_Oberlausitz_Neugersdorf");
        Assert.assertEquals(processed.getPredicate().getId(), FactClassifierProcessor.CLASS_RELATION_STR);
        Assert.assertEquals(processed.getObject().getId(),
                "http://dbpedia.org/resource/Category:Football_clubs_in_Germany");
    }


    @Test
    public void duplicateProcessorTest() {
        KBId kbId = KBId.of("obama", "facts");
        PropertyEntity prop = ModelUtils.createProperty("http://dbpedia.org/property/president");
        Processor processor = Processors.create(config.withOnlyPath("sink-duplicate"));
        Holder holder = new Indexable(prop, kbId);
        processor.run(holder);
        Assert.assertFalse(holder.isSinkable());
        processor.run(holder);
        Assert.assertTrue(holder.isSinkable());
    }

    @Test(expectedExceptions = FatalProcessorException.class)
    public void unrecoverableErrorProcessorTest() {
        KBId kbId = KBId.of("any", "type");
        Processor processor = Processors.create(config.withOnlyPath(CoreferenceResolutionProcessor.name));
        Holder holder = new Indexable(new Document("Some Id", " Some title", "A bunch of text .."), kbId);
        processor.run(holder);
        Assert.assertFalse(holder.isSinkable());
    }

}
