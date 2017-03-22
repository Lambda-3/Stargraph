package net.stargraph.core.qa.nli;

import net.stargraph.core.qa.annotator.Word;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public final class QuestionAnalysis {
    private QueryType queryType;
    private String question;
    private List<Word> annotatedWords;
    private Deque<AnalysisStep> steps;

    QuestionAnalysis(String question, QueryType queryType) {
        this.question = Objects.requireNonNull(question);
        this.queryType = Objects.requireNonNull(queryType);
        this.steps = new ArrayDeque<>();
    }

    void annotate(List<Word> annotatedWords) {
        this.annotatedWords = Objects.requireNonNull(annotatedWords);
        this.steps.add(new AnalysisStep(annotatedWords));
    }

    void resolve(List<DataModelTypePattern> rules) {
        rules.forEach(rule -> {
            AnalysisStep step = steps.peek().resolve(rule);
            if (step != null) {
                steps.push(step);
            }
        });
    }

    void clean(List<Pattern> stopPatterns) {
        AnalysisStep step = steps.peek().clean(stopPatterns);
        if (step != null) {
            steps.push(step);
        }
    }

    public AnalysisStep getResult() {
        return steps.peek();
    }

    public QueryType getQueryType() {
        return queryType;
    }

    @Override
    public String toString() {
        return "Analysis{" +
                "q='" + question + '\'' +
                ", queryType'" + queryType + '\'' +
                ", POS=" + annotatedWords +
                ", Bindings='" + getResult().getBindings() + '\'' +
                '}';
    }
}
