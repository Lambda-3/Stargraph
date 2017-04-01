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
import net.stargraph.core.Stargraph;
import net.stargraph.core.query.AnswerSet;
import net.stargraph.core.query.QueryEngine;
import net.stargraph.model.InstanceEntity;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

public class NaturalLanguageInterfaceIT {
    String testConfigFilePath = System.getProperty("stargraph.configFile");
    QueryEngine queryEngine;

    @BeforeClass
    public void beforeClass() {
        if (testConfigFilePath == null) {
            Assert.fail("Configure Java System Property 'stargraph.configFile'");
        }
        Config conf = Stargraph.readConfiguration(new File(testConfigFilePath));
        queryEngine = new QueryEngine("dbpedia-2016", new Stargraph(conf, true));
    }

    @Test
    public void q0() {
        AnswerSet answerSet = (AnswerSet) queryEngine.query("Who is the wife of Barack Obama?");
        Assert.assertTrue(answerSet.getShortAnswer().contains(new InstanceEntity("dbr:Michelle_Obama", "Michelle Obama")));
    }
}
