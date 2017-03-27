package net.stargraph.test;

import com.typesafe.config.ConfigFactory;
import net.stargraph.Language;
import net.stargraph.core.query.Analyzers;
import net.stargraph.core.query.SPARQLQueryBuilder;
import net.stargraph.core.query.QueryType;
import net.stargraph.core.query.nli.QuestionAnalysis;
import net.stargraph.core.query.nli.QuestionAnalyzer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public final class NaturalLanguageInterfaceTest {

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

    @Test
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
