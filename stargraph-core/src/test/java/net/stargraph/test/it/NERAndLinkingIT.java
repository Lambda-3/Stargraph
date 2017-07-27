package net.stargraph.test.it;

import com.typesafe.config.ConfigFactory;
import net.stargraph.ModelUtils;
import net.stargraph.core.Stargraph;
import net.stargraph.core.ner.LinkedNamedEntity;
import net.stargraph.core.ner.NER;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public final class NERAndLinkingIT {
    NER ner;

    @BeforeClass
    public void beforeClass() throws Exception {
        ConfigFactory.invalidateCaches();
        Stargraph core = new Stargraph();
        ner = core.getNER("dbpedia-2016");
        Assert.assertNotNull(ner);
    }

    @Test
    public void linkObamaTest() {
        List<LinkedNamedEntity> entities = ner.searchAndLink("Barack Obama");
        Assert.assertEquals(entities.get(0).getEntity(), ModelUtils.createInstance("dbr:Barack_Obama"));
    }

    @Test
    public void NoEntitiesTest() {
        final String text = "Moreover, they were clearly meant to be exemplary invitations to revolt. And of course this will not make any sense.";
        List<LinkedNamedEntity> entities = ner.searchAndLink(text);
        Assert.assertTrue(entities.isEmpty());
    }

    @Test
    public void linkTest() {
        final String text = "What it Really Stands for Anarchy '' in Anarchism and Other Essays.Individualist anarchist " +
                "Benjamin Tucker defined anarchism as opposition to authority as follows `` They found that they must " +
                "turn either to the right or to the left , -- follow either the path of Authority or the path of Liberty .";

        List<LinkedNamedEntity> entities = ner.searchAndLink(text);
        System.out.println(entities);
    }
}