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

import net.stargraph.core.Stargraph;
import net.stargraph.core.query.response.AnswerSetResponse;
import net.stargraph.core.query.QueryEngine;
import net.stargraph.core.query.response.SPARQLSelectResponse;
import net.stargraph.model.InstanceEntity;
import net.stargraph.model.ValueEntity;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class QueryEngineIT {
    QueryEngine queryEngine;

    @BeforeClass
    public void beforeClass() {
        queryEngine = new QueryEngine("dbpedia-2016", new Stargraph());
    }

    @Test
    public void nli0Test() {
        AnswerSetResponse response = (AnswerSetResponse) queryEngine.query("Who is the wife of Barack Obama?");
        Assert.assertTrue(response.getShortAnswer().contains(new InstanceEntity("http://dbpedia.org/resource/Michelle_Obama", "Michelle Obama")));
    }

    @Test
    public void nli1Test() {
        AnswerSetResponse response = (AnswerSetResponse) queryEngine.query("How tall is Michael Jordan?");
        Assert.assertTrue(response.getShortAnswer().contains(new ValueEntity("6", "http://www.w3.org/2001/XMLSchema#integer", null)));
    }

    @Test(enabled = false)
    public void nli2Test() {
        AnswerSetResponse response = (AnswerSetResponse) queryEngine.query("Give me all movies directed by Francis Ford Copolla.");
        //Assert.assertTrue(answerSet.getShortAnswer().contains(new InstanceEntity("dbr:Michelle_Obama", "Michelle Obama")));
    }

    @Test
    public void sparql0Test() {
        SPARQLSelectResponse response  = (SPARQLSelectResponse) queryEngine.query("SELECT ?o WHERE " +
                "{ <http://dbpedia.org/resource/Barack_Obama> <http://xmlns.com/foaf/0.1/depiction> ?o }");
        Assert.assertEquals("http://commons.wikimedia.org/wiki/Special:FilePath/President_Barack_Obama.jpg",
                response.getBindings().get("o").get(0).getId());
    }
}
