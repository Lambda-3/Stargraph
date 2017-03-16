package net.stargraph.core.qa.nli;

import net.stargraph.core.qa.annotator.Word;

import java.util.List;
import java.util.Objects;

public final class AnalyzedQuestion {
    private String question;
    private List<Word> annotatedWords;

    public AnalyzedQuestion(String question) {
        this.question = Objects.requireNonNull(question);
    }

    void addAnnotations(List<Word> annotatedWords) {
        this.annotatedWords = Objects.requireNonNull(annotatedWords);
    }

    @Override
    public String toString() {
        return "AnalyzedQuestion{" +
                "question='" + question + '\'' +
                ", annotatedWords=" + annotatedWords +
                '}';
    }
}
