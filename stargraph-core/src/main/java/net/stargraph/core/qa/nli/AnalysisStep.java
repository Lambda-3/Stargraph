package net.stargraph.core.qa.nli;

import net.stargraph.core.qa.annotator.Word;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class AnalysisStep {
    private static final Pattern punctPattern = Pattern.compile("[(){},.;!?<>%]");

    private List<Word> annotated;
    private String questionStr;
    private String posTagStr;
    private List<DataModelBinding> dataModelBindings;

    private AnalysisStep(List<DataModelBinding> dataModelBindings, List<Word> annotated, String questionStr, String posTagStr) {
        this.dataModelBindings = Objects.requireNonNull(dataModelBindings);
        this.annotated = Objects.requireNonNull(annotated);
        this.questionStr = Objects.requireNonNull(questionStr);
        this.posTagStr = Objects.requireNonNull(posTagStr);

        if (annotated.isEmpty() || questionStr.isEmpty() || posTagStr.isEmpty()) {
            throw new IllegalArgumentException();
        }
    }

    public AnalysisStep(List<Word> annotated) {
        this(Collections.emptyList(), Objects.requireNonNull(annotated),
                annotated.stream().map(Word::getText).collect(Collectors.joining(" ")),
                annotated.stream().map(w -> w.getPosTag().getTag()).collect(Collectors.joining(" ")));
    }

    public List<DataModelBinding> getBindings() {
        return Arrays.stream(questionStr.split("\\s"))
                .map(this::getBinding)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    AnalysisStep clean(List<Pattern> stopPatterns) {

        stopPatterns.forEach(pattern -> {
            Matcher matcher = pattern.matcher(questionStr);
            if (matcher.matches()) {
                questionStr = replace(pattern, questionStr, "").value;
            }
        });

        questionStr = compact(questionStr);

        return new AnalysisStep(dataModelBindings, annotated, questionStr, posTagStr);
    }

    AnalysisStep resolve(DataModelTypePattern rule) {
        final DataModelType modelType = Objects.requireNonNull(rule).getDataModelType();
        final Pattern rulePattern = Pattern.compile(rule.getPattern());

        final List<DataModelBinding> bindings = new LinkedList<>(dataModelBindings);

        if (matches(rulePattern)) {
            String newQuestionStr;
            String newPosTagStr = posTagStr;

            if (rule.isLexical()) {
                String placeHolder = createPlaceholder(questionStr, modelType);
                Replacement replacement = replace(rulePattern, questionStr, placeHolder);
                newQuestionStr = replacement.value;
            }
            else {
                String placeHolder = createPlaceholder(posTagStr, modelType);
                Replacement posTagReplacement = replace(rulePattern, posTagStr, placeHolder);
                newPosTagStr = posTagReplacement.value;

                String subStr = findSubStr(posTagReplacement);
                Replacement questionReplacement = replace(subStr, questionStr, placeHolder);
                newQuestionStr = questionReplacement.value;

                bindings.add(new DataModelBinding(modelType, subStr, placeHolder));
            }

            return new AnalysisStep(bindings, annotated, newQuestionStr, newPosTagStr);
        }

        return null;
    }

    private String findSubStr(Replacement replacement) {
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

        return subStr;
    }

    private boolean matches(Pattern pattern) {
        Matcher m1 = pattern.matcher(posTagStr);
        Matcher m2 = pattern.matcher(questionStr);
        return m1.matches() || m2.matches();
    }

    private Replacement replace(String subStr, String target, String replacementStr) {
        return replace(Pattern.compile(String.format("^.+(%s).+$", Objects.requireNonNull(subStr))), target, replacementStr);
    }

    private Replacement replace(Pattern pattern, String target, String replacementStr) {
        Matcher matcher = pattern.matcher(target);
        if (matcher.matches()) {
            // As we expect just one capture capture per pattern this will replaceWithModelType the capture by the desired replacement.
            StringBuffer sb = new StringBuffer();
            String capturedStr = matcher.group(1);
            matcher.appendReplacement(sb, matcher.group(0).replaceFirst(Pattern.quote(capturedStr), replacementStr));
            matcher.appendTail(sb);
            return new Replacement(sb.toString(), capturedStr);
        }
        return new Replacement(target, null);
    }

    private String createPlaceholder(String target, DataModelType modelType) {
        int unusedIdx = 1;
        String placeHolder = String.format("%s_%d", modelType.name(), unusedIdx);
        while (target.contains(placeHolder)) {
            placeHolder = String.format("%s_%d", modelType.name(), unusedIdx++);
        }
        return placeHolder;
    }

    private String compact(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        String punctLess = punctPattern.matcher(str).replaceAll(" ");

        return Arrays.stream(punctLess.split("\\s")).map(String::trim)
                .filter(s -> !s.isEmpty()).collect(Collectors.joining(" "));
    }

    private Optional<DataModelBinding> getBinding(String term) {
        return dataModelBindings.stream().filter(binding -> binding.getPlaceHolder().equals(term)).findFirst();
    }

    @Override
    public String toString() {
        return "Step{" +
                "questionStr='" + questionStr + '\'' +
                ", posTagStr='" + posTagStr + '\'' +
                '}';
    }

    private static class Replacement {
        final String value;
        final String capture;

        Replacement(String value, String capture) {
            this.value = value;
            this.capture = capture;
        }
    }
}
