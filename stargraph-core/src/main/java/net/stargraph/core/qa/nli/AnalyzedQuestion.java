package net.stargraph.core.qa.nli;

import net.stargraph.core.qa.annotator.Word;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

public final class AnalyzedQuestion {
    private String question;
    private List<Word> annotatedWords;
    private Deque<QuestionView> views;

    public AnalyzedQuestion(String question) {
        this.question = Objects.requireNonNull(question);
        this.views = new ArrayDeque<>();
    }

    void addAnnotations(List<Word> annotatedWords) {
        this.annotatedWords = Objects.requireNonNull(annotatedWords);
        this.views.add(new QuestionView(annotatedWords));
    }

    void transform(DataModelTypePattern rule) {
        QuestionView newView = views.peek().transform(rule);
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
