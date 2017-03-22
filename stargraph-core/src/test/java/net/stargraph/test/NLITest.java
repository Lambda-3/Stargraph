package net.stargraph.test;

import com.typesafe.config.ConfigFactory;
import net.stargraph.Language;
import net.stargraph.core.query.Analyzers;
import net.stargraph.core.query.nli.QuestionAnalysis;
import net.stargraph.core.query.nli.QuestionAnalyzer;
import org.testng.annotations.Test;

public final class NLITest {

    @Test
    public void q0() {
        Analyzers analyzers = new Analyzers(ConfigFactory.load().getConfig("stargraph"));
        QuestionAnalyzer analyzer = analyzers.getQuestionAnalyzer(Language.EN);
        QuestionAnalysis analyzed = analyzer.analyse("Who is the wife of Barack Obama?");
        System.out.println(analyzed.getSAQuery());
    }

}
