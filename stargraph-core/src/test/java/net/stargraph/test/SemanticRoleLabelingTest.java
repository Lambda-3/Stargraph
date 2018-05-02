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
import net.stargraph.query.Language;
import net.stargraph.core.query.Analyzers;
import net.stargraph.core.query.SPARQLQueryBuilder;
import net.stargraph.core.query.QueryType;
import net.stargraph.core.query.nli.QuestionAnalysis;
import net.stargraph.core.query.nli.QuestionAnalyzer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public final class  SemanticRoleLabelingTest {

    QuestionAnalyzer analyzer;

    @BeforeClass
    public void beforeClass() {
        Analyzers analyzers = new Analyzers(ConfigFactory.load().getConfig("stargraph"));
        analyzer = analyzers.getQuestionAnalyzer(Language.EN);
    }

    @Test
    public void q0() {
        QuestionAnalysis analyzed = analyzer.analyse("Who is the wife of Barack Obama?");
        SPARQLQueryBuilder builder = analyzed.getSPARQLQueryBuilder();
        Assert.assertEquals(builder.getQueryType(), QueryType.SELECT);
        Assert.assertEquals(builder.getTriplePatterns().getPlanId(), "CLASS_1 INSTANCE_1");
        Assert.assertEquals(builder.getBinding("CLASS_1").getTerm(), "wife");
        Assert.assertEquals(builder.getBinding("INSTANCE_1").getTerm(), "Barack Obama");
    }

    @Test(enabled = false) //todo: re-enable
    public void q1() {
        QuestionAnalysis analyzed = analyzer.analyse("Give me all movies directed by Francis Ford Coppola");
        SPARQLQueryBuilder builder = analyzed.getSPARQLQueryBuilder();
        Assert.assertEquals(builder.getQueryType(), QueryType.SELECT);
        Assert.assertEquals(builder.getTriplePatterns().getPlanId(), "CLASS_1 PROPERTY_1 by INSTANCE_1");
        Assert.assertEquals(builder.getBinding("CLASS_1").getTerm(), "movies");
        Assert.assertEquals(builder.getBinding("INSTANCE_1").getTerm(), "Francis Ford Coppola");
        Assert.assertEquals(builder.getBinding("PROPERTY_1").getTerm(), "directed");
    }

}
