package net.stargraph.core.query.nli;

import net.stargraph.Language;
import net.stargraph.UnmappedQueryTypeException;
import net.stargraph.core.query.Rules;
import net.stargraph.core.query.annotator.Annotator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public final class QuestionAnalyzer {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Marker marker = MarkerFactory.getMarker("nli");
    private Language language;
    private Annotator annotator;
    private List<DataModelTypePattern> dataModelTypePatterns;
    private List<QueryPlanPattern> queryPlanPatterns;
    private List<Pattern> stopPatterns;
    private List<QueryTypePattern> queryTypePatterns;

    public QuestionAnalyzer(Language language, Annotator annotator, Rules rules) {
        logger.info(marker, "Creating analyzer for '{}'", language);
        this.language = Objects.requireNonNull(language);
        this.annotator = Objects.requireNonNull(annotator);
        this.dataModelTypePatterns = rules.getDataModelTypeRules(language);
        this.queryPlanPatterns = rules.getQueryPlanRules(language);
        this.stopPatterns = rules.getStopRules(language);
        this.queryTypePatterns = rules.getQueryTypeRules(language);
    }

    public QuestionAnalysis analyse(String question) {
        long startTime = System.currentTimeMillis();
        QuestionAnalysis analysis = new QuestionAnalysis(question, selectQueryType(question));
        analysis.annotate(annotator.run(language, question));
        analysis.resolveDataModelBindings(dataModelTypePatterns);
        analysis.clean(stopPatterns);
        analysis.resolveSPARQL(queryPlanPatterns);
        logger.info(marker, "{}", getTimingReport(question, startTime));
        return analysis;
    }

    private QueryType selectQueryType(String question) {
        return queryTypePatterns.stream()
                .filter(p -> p.match(question))
                .map(QueryTypePattern::getQueryType)
                .findFirst().orElseThrow(() -> new UnmappedQueryTypeException(question));
    }

    private String getTimingReport(String q, long start) {
        long elapsedTime = System.currentTimeMillis() - start;
        return String.format("'%s' analyzed in %.3fs", q, elapsedTime / 1000.0);
    }

}
