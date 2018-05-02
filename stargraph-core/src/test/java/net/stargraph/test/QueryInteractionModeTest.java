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

import com.typesafe.config.ConfigFactory;
import net.stargraph.core.query.InteractionModeSelector;
import net.stargraph.query.InteractionMode;
import net.stargraph.query.Language;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test all kinds of accepted queries by the Query Engine.
 */
public final class QueryInteractionModeTest {

    InteractionModeSelector selector;

    @BeforeClass
    public void beforeClass() {
        selector = new InteractionModeSelector(ConfigFactory.load().getConfig("stargraph"), Language.EN);
    }

    @Test
    public void nliTest() {
        InteractionMode mode = selector.detect("How tall is Michael Jordan?");
        Assert.assertEquals(mode, InteractionMode.NLI);
    }

    @Test
    public void sparqlTest() {
        InteractionMode mode = selector.detect("SELECT ?o WHERE { <http://dbpedia.org/resource/Barack_Obama> <http://xmlns.com/foaf/0.1/depiction> ?o }");
        Assert.assertEquals(mode, InteractionMode.SPARQL);
    }

}
