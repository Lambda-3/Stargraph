package net.stargraph.core.qa.nli;

import net.stargraph.core.qa.annotator.Word;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public final class AnalyzedQuestion {
    private String question;
    private List<Word> annotatedWords;
    private Deque<QuestionView> views;

    public AnalyzedQuestion(String question) {
        this.question = Objects.requireNonNull(question);
        this.views = new ArrayDeque<>();
    }

    void annotate(List<Word> annotatedWords) {
        this.annotatedWords = Objects.requireNonNull(annotatedWords);
        this.views.add(new QuestionView(annotatedWords));
    }

    void resolve(List<DataModelTypePattern> rules) {
        rules.forEach(rule -> {
            QuestionView newView = views.peek().resolve(rule);
            if (newView != null) {
                views.push(newView);
            }
        });
    }

    void clean(List<Pattern> stopPatterns) {
        QuestionView newView = views.peek().clean(stopPatterns);
        if (newView != null) {
            views.push(newView);
        }
    }

    public QuestionView getView() {
        return views.peek();
    }

    @Override
    public String toString() {
        return "AnalyzedQuestion{" +
                "question='" + question + '\'' +
                ", annotatedWords=" + annotatedWords +
                ", posView='" + views.peek() + '\'' +
                '}';
    }
}
