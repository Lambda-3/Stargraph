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
import net.stargraph.core.query.QueryEngine;
import net.stargraph.core.query.response.AnswerSetResponse;
import net.stargraph.core.query.response.SPARQLSelectResponse;
import net.stargraph.model.InstanceEntity;
import net.stargraph.model.LabeledEntity;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.json.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class QueryEngineIT {
    private static String dbId = "dbpedia-2016";
    private QueryEngine queryEngine;

    @BeforeClass
    public void beforeClass() {
        queryEngine = new QueryEngine(dbId, new Stargraph());
    }

    @Test
    public void whoIsTheWifeOfBarackObamaTest() {
        AnswerSetResponse response = (AnswerSetResponse) queryEngine.query("Who is the wife of Barack Obama?");
        Assert.assertTrue(response.getShortAnswer().contains(new InstanceEntity("http://dbpedia.org/resource/Michelle_Obama", "Michelle Obama")));
    }

    @Test
    public void sparqlSelectTest() {
        SPARQLSelectResponse response  = (SPARQLSelectResponse) queryEngine.query("SELECT ?o WHERE " +
                "{ <http://dbpedia.org/resource/Barack_Obama> <http://xmlns.com/foaf/0.1/depiction> ?o }");
        Assert.assertEquals("http://commons.wikimedia.org/wiki/Special:FilePath/President_Barack_Obama.jpg",
                response.getBindings().get("o").get(0).getId());
    }

    @Test(dataProvider = "nlQueries", dataProviderClass = QueryEngineIT.class)
    public void test(String q, List<String> answers) {
        AnswerSetResponse response = (AnswerSetResponse) queryEngine.query(q);
        List<String> answerIds = response.getShortAnswer().stream().map(LabeledEntity::getId).collect(Collectors.toList());
        if (!answers.stream().anyMatch(answerIds::contains)) {
            Assert.fail("Test fail for query '" + q + "'. Accepted Answer Set: " + answers);
        }
    }

    @DataProvider(name = "nlQueries")
    public static Iterator<Object[]> createTestSet() throws Exception {
        List<Object[]> data = new ArrayList<>();
        try (InputStream is = ClassLoader.getSystemResourceAsStream(dbId + "-test-queries.json")) {
            JsonReader reader = Json.createReader(is);
            JsonArray arr = reader.readArray();
            arr.getValuesAs(JsonObject.class).forEach(entry -> {
                JsonArray acceptedArr = entry.getJsonArray("accepted");
                List<String> l = acceptedArr.getValuesAs(JsonString.class)
                        .stream().map(JsonString::getString).collect(Collectors.toList());
                data.add(new Object[] { entry.getString("query"), l});
            });
            return data.iterator();
        }
    }
}
