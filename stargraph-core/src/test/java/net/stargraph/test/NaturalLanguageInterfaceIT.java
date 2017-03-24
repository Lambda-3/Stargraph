package net.stargraph.test;

import net.stargraph.core.Stargraph;
import net.stargraph.core.query.QueryEngine;
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
        queryEngine = new QueryEngine(Stargraph.readConfiguration(new File(testConfigFilePath)));
    }

    @Test
    public void q0() {
        Assert.assertFalse(false);
    }
}
