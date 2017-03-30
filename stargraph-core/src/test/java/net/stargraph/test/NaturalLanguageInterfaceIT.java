package net.stargraph.test;

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
