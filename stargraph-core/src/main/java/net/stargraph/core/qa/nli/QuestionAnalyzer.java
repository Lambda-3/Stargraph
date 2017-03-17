package net.stargraph.core.qa.nli;

import net.stargraph.Language;
import net.stargraph.core.qa.Rules;
import net.stargraph.core.qa.annotator.Annotator;

import java.util.List;
import java.util.Objects;

public final class QuestionAnalyzer {
    private Language language;
    private Annotator annotator;
    private List<DataModelTypePattern> dataModelTypePatterns;
    private List<QueryPlanPattern> queryPlanPatterns;

    public QuestionAnalyzer(Language language, Annotator annotator, Rules rules) {
        this.language = Objects.requireNonNull(language);
        this.annotator = Objects.requireNonNull(annotator);
        this.dataModelTypePatterns = rules.getDataModelTypeRules(language);
        this.queryPlanPatterns = rules.getQueryPlanRules(language);
    }

    public AnalyzedQuestion analyse(String question) {
        AnalyzedQuestion analyzed = new AnalyzedQuestion(question);
        analyzed.addAnnotations(annotator.run(language, question));
        return analyzed;
    }

}
