package net.stargraph.core.query.nli;

/*-
 * ==========================License-Start=============================
 * stargraph-core
 * --------------------------------------------------------------------
 * Copyright (C) 2017 Lambda^3
 * --------------------------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ==========================License-End===============================
 */

import net.stargraph.query.Language;
import net.stargraph.StarGraphException;
import net.stargraph.UnmappedQueryTypeException;
import net.stargraph.core.query.QueryType;
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
    private List<QueryPlanPatterns> queryPlanPatterns;
    private List<Pattern> stopPatterns;
    private List<QueryTypePatterns> queryTypePatterns;

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
        QuestionAnalysis analysis = null;
        try {
            long startTime = System.currentTimeMillis();
            analysis = new QuestionAnalysis(question, selectQueryType(question));
            analysis.annotate(annotator.run(language, question));
            analysis.resolveDataModelBindings(dataModelTypePatterns);
            analysis.clean(stopPatterns);
            analysis.resolveSPARQL(queryPlanPatterns);
            logger.info(marker, "{}", getTimingReport(question, startTime));
            return analysis;
        }
        catch (Exception e) {
            logger.error(marker, "Analysis failure. Last step: {}", analysis);
            throw new StarGraphException(e);
        }
    }

    private QueryType selectQueryType(String question) {
        return queryTypePatterns.stream()
                .filter(p -> p.match(question))
                .map(QueryTypePatterns::getQueryType)
                .findFirst().orElseThrow(() -> new UnmappedQueryTypeException(question));
    }

    private String getTimingReport(String q, long start) {
        long elapsedTime = System.currentTimeMillis() - start;
        return String.format("'%s' analyzed in %.3fs", q, elapsedTime / 1000.0);
    }

}
