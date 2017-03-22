package net.stargraph.core.query.nli;

import net.stargraph.core.query.agnostic.SchemaAgnosticSPARQL;
import net.stargraph.core.query.annotator.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class QuestionAnalysis {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Marker marker = MarkerFactory.getMarker("nli");

    private QueryType queryType;
    private String question;
    private List<Word> annotatedWords;
    private Deque<AnalysisStep> steps;
    private SchemaAgnosticSPARQL saSPARQL;

    QuestionAnalysis(String question, QueryType queryType) {
        this.question = Objects.requireNonNull(question);
        this.queryType = Objects.requireNonNull(queryType);
        this.steps = new ArrayDeque<>();
        logger.info(marker, "Analyzing '{}', detected type is '{}'", question, queryType);
    }

    void annotate(List<Word> annotatedWords) {
        this.annotatedWords = Objects.requireNonNull(annotatedWords);
        this.steps.add(new AnalysisStep(annotatedWords));
    }

    void resolveDataModelBindings(List<DataModelTypePattern> rules) {
        if (steps.isEmpty()) {
            throw new IllegalStateException();
        }

        logger.info(marker, "Resolving Data Models");
        rules.forEach(rule -> {
            AnalysisStep step = steps.peek().resolve(rule);
            if (step != null) {
                steps.push(step);
            }
        });
    }

    void clean(List<Pattern> stopPatterns) {
        if (steps.isEmpty()) {
            throw new IllegalStateException();
        }

        logger.info(marker, "Cleaning up");
        AnalysisStep step = steps.peek().clean(stopPatterns);
        if (step != null) {
            steps.push(step);
        }
    }

    void resolveSchemaAgnosticQuery(List<QueryPlanPattern> rules) {
        if (steps.isEmpty()) {
            throw new IllegalStateException();
        }

        List<DataModelBinding> bindings = steps.peek().getBindings();
        String planId = bindings.stream().map(DataModelBinding::getPlaceHolder).collect(Collectors.joining(" "));
        logger.info(marker, "Creating SA Query, plan is '{}'", planId);
        //
    }

    public SchemaAgnosticSPARQL getSAQuery() {
        if (saSPARQL == null) {
            throw new IllegalStateException();
        }
        return saSPARQL;
    }

    @Override
    public String toString() {
        return "Analysis{" +
                "q='" + question + '\'' +
                ", queryType'" + queryType + '\'' +
                ", POS=" + annotatedWords +
                ", Bindings='" + steps.peek().getBindings() + '\'' +
                '}';
    }
}
