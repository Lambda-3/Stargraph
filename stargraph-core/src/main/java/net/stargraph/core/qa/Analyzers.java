package net.stargraph.core.qa;

import com.typesafe.config.Config;
import net.stargraph.Language;
import net.stargraph.core.impl.opennlp.OpenNLPAnnotator;
import net.stargraph.core.qa.annotator.Annotator;
import net.stargraph.core.qa.nli.QuestionAnalyzer;

import java.util.concurrent.ConcurrentHashMap;

public final class Analyzers {
    private Rules rules;
    private Annotator annotator;
    private ConcurrentHashMap<Language, QuestionAnalyzer> questionAnalyzers;

    public Analyzers(Config config) {
        this.rules = new Rules(config);
        this.annotator = new OpenNLPAnnotator(config); // future decoupling point
        this.questionAnalyzers = new ConcurrentHashMap<>();
    }

    public QuestionAnalyzer getQuestionAnalyzer(Language language) {
        return questionAnalyzers.computeIfAbsent(language, lang -> new QuestionAnalyzer(lang, annotator, rules));
    }

}
