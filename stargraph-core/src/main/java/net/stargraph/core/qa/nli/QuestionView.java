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

    private QuestionView(List<Word> annotated, String questionStr, String posTagStr) {
        this.annotated = Objects.requireNonNull(annotated);
        this.questionStr = Objects.requireNonNull(questionStr);
        this.posTagStr = Objects.requireNonNull(posTagStr);
    }

    public QuestionView(List<Word> annotated) {
        this(Objects.requireNonNull(annotated),
                annotated.stream().map(Word::getText).collect(Collectors.joining(" ")),
                annotated.stream().map(w -> w.getPosTag().getTag()).collect(Collectors.joining(" ")));
    }

    public String getQuestion() {
        return questionStr;
    }

    public String getPosTag() {
        return posTagStr;
    }

    public QuestionView transform(DataModelTypePattern rule) {
        final DataModelType modelType = Objects.requireNonNull(rule).getDataModelType();
        final Pattern rulePattern = Pattern.compile(rule.getPattern());

        if (matches(rulePattern)) {
            String newQuestionStr;
            String newPosTagStr = posTagStr;

            if (rule.isLexical()) {
                Replacement<String, String> replacement = replace(rulePattern, questionStr, modelType);
                newQuestionStr = replacement.value;
            }
            else {
                Replacement<String, String> posTagReplacement = replace(rulePattern, posTagStr, modelType);
                newPosTagStr = posTagReplacement.value;

                Pattern qPattern = findQuestionPattern(posTagReplacement);
                Replacement<String, String> questionReplacement = replace(qPattern, questionStr, modelType);
                newQuestionStr = questionReplacement.value;
            }

            return new QuestionView(annotated, newQuestionStr, newPosTagStr);
        }

        return null;
    }

    private Pattern findQuestionPattern(Replacement<String, String> replacement) {
        String[] capture = Objects.requireNonNull(replacement).capture.split("\\s");
        int startIdx = 0;
        String subStr = null;
        for (Word w : annotated) {
            if (w.getPosTag().getTag().equals(capture[0])) {
                subStr = annotated.stream()
                        .skip(startIdx)
                        .limit(capture.length)
                        .map(Word::getText).collect(Collectors.joining(" "));
                break;
            }
            startIdx++;
        }

        return subStr != null ? Pattern.compile(String.format("^.+(%s).+$", subStr)) : null;
    }

    private boolean matches(Pattern pattern) {
        Matcher m1 = pattern.matcher(posTagStr);
        Matcher m2 = pattern.matcher(questionStr);
        return m1.matches() || m2.matches();
    }

    private Replacement<String, String> replace(Pattern pattern, String target, DataModelType modelType) {
        String placeHolder = createPlaceholder(target, modelType);
        Matcher matcher = pattern.matcher(target);
        if (matcher.matches()) {
            // As we expect just one capture capture per pattern this will replace the capture by the desired replacement.
            StringBuffer sb = new StringBuffer();
            String capturedStr = matcher.group(1);
            matcher.appendReplacement(sb, matcher.group(0).replaceFirst(Pattern.quote(capturedStr), placeHolder));
            matcher.appendTail(sb);
            return new Replacement<>(sb.toString(), capturedStr);
        }
        return new Replacement<>(target, null);
    }

    private String createPlaceholder(String target, DataModelType modelType) {
        int unusedIdx = 1;
        String placeHolder = String.format("%s_%d", modelType.name(), unusedIdx);
        while (target.contains(placeHolder)) {
            placeHolder = String.format("%s_%d", modelType.name(), unusedIdx++);
        }
        return placeHolder;
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
