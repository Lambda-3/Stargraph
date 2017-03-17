package net.stargraph.core.qa.nli;

import net.stargraph.core.qa.annotator.Word;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class QuestionView {
    private List<Word> annotated;
    private String questionStr;
    private String posTagStr;

    private QuestionView(String questionStr, String posTagStr) {
        this.questionStr = Objects.requireNonNull(questionStr);
        this.posTagStr = Objects.requireNonNull(posTagStr);
    }

    public QuestionView(List<Word> annotated) {
        this(Objects.requireNonNull(annotated).stream().map(Word::getText).collect(Collectors.joining(" ")),
                Objects.requireNonNull(annotated).stream().map(w -> w.getPosTag().getTag()).collect(Collectors.joining(" ")));
        this.annotated = annotated;
    }

    public String getQuestion() {
        return questionStr;
    }

    public String getPosTag() {
        return posTagStr;
    }

    public QuestionView transform(DataModelTypePattern rule) {
        if (rule == null) {
            throw new IllegalArgumentException("Can't update with null rule");
        }

        final String typeStr = rule.getDataModelType().name();
        final Pattern rulePattern = Pattern.compile(rule.getPattern());

        if (matches(rulePattern)) {
            String newQuestionStr = questionStr;
            String newPosTagStr = posTagStr;

            if (rule.isLexical()) {
                Replacement<String, String> replacement = replace(rulePattern, questionStr, typeStr);
                newQuestionStr = replacement.value;
            }
            else {
                Replacement<String, String> replacement = replace(rulePattern, posTagStr, typeStr);
                newPosTagStr = replacement.value;

                Word word = annotated.stream()
                        .filter(w -> w.getPosTag().getTag().equals(replacement.capture))
                        .findFirst().orElseThrow(IllegalStateException::new);

                System.out.println(word);

            }

            return new QuestionView(newQuestionStr, newPosTagStr);
        }

        return null;
    }

    private boolean matches(Pattern pattern) {
        Matcher m1 = pattern.matcher(posTagStr);
        Matcher m2 = pattern.matcher(questionStr);
        return m1.matches() || m2.matches();
    }

    private Replacement<String, String> replace(Pattern pattern, String target, String replacement) {
        Matcher matcher = pattern.matcher(target);
        if (matcher.matches()) {
            // As we expect just one capture capture per pattern this will replace the capture by the desired replacement.
            StringBuffer sb = new StringBuffer();
            String capturedStr = matcher.group(1);
            matcher.appendReplacement(sb, matcher.group(0).replaceFirst(Pattern.quote(capturedStr), replacement));
            matcher.appendTail(sb);
            return new Replacement<>(sb.toString(), capturedStr);
        }
        return new Replacement<>(target, null);
    }

    @Override
    public String toString() {
        return "QuestionView{" +
                "questionStr='" + questionStr + '\'' +
                ", posTagStr='" + posTagStr + '\'' +
                '}';
    }

    private static class Replacement<S, T> {
        final S value;
        final T capture;

        Replacement(S value, T capture) {
            this.value = value;
            this.capture = capture;
        }
    }
}
