package net.stargraph.test;

import com.typesafe.config.ConfigFactory;
import net.stargraph.Language;
import net.stargraph.core.query.Analyzers;
import net.stargraph.core.query.SPARQLQueryBuilder;
import net.stargraph.core.query.nli.QueryType;
import net.stargraph.core.query.nli.QuestionAnalysis;
import net.stargraph.core.query.nli.QuestionAnalyzer;
import org.testng.Assert;
import org.testng.annotations.Test;

public final class NLITest {

    @Test
    public void q0() {
        Analyzers analyzers = new Analyzers(ConfigFactory.load().getConfig("stargraph"));
        QuestionAnalyzer analyzer = analyzers.getQuestionAnalyzer(Language.EN);
        QuestionAnalysis analyzed = analyzer.analyse("Who is the wife of Barack Obama?");
        SPARQLQueryBuilder builder = analyzed.getSPARQLQueryBuilder();
        Assert.assertEquals(builder.getQueryType(), QueryType.SELECT);
        Assert.assertEquals(builder.getTriplePatterns().getPlanId(), "CLASS_1 INSTANCE_1");
        Assert.assertEquals(builder.getBinding("CLASS_1").getTerm(), "wife");
        Assert.assertEquals(builder.getBinding("INSTANCE_1").getTerm(), "Barack Obama");
    }

}
