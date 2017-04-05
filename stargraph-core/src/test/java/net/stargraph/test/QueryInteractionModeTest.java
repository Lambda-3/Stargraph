package net.stargraph.test;

import com.typesafe.config.ConfigFactory;
import net.stargraph.core.query.InterationModeSelector;
import net.stargraph.query.InteractionMode;
import net.stargraph.query.Language;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test all kinds of accepted queries by the Query Engine.
 */
public final class QueryInteractionModeTest {

    InterationModeSelector selector;

    @BeforeClass
    public void beforeClass() {
        selector = new InterationModeSelector(ConfigFactory.load().getConfig("stargraph"), Language.EN);
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
