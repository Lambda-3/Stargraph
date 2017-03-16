package net.stargraph.core.qa.nli;

import net.stargraph.Language;
import net.stargraph.core.qa.Rules;
import net.stargraph.core.qa.annotator.Annotator;

import java.util.List;
import java.util.Objects;

public final class QuestionAnalyzer {
    private Language language;
    private Annotator annotator;
    private List<SyntaticRule> syntaticRules;
    private List<QueryPlan> queryPlans;

    public QuestionAnalyzer(Language language, Annotator annotator, Rules rules) {
        this.language = Objects.requireNonNull(language);
        this.annotator = Objects.requireNonNull(annotator);
        this.syntaticRules = rules.getSyntaticRules(language);
        this.queryPlans = rules.getQueryPlans(language);
    }

    public AnalyzedQuestion analyse(String question) {
        AnalyzedQuestion analyzed = new AnalyzedQuestion(question);
        analyzed.addAnnotations(annotator.run(language, question));
        return analyzed;
    }

}
