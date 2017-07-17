package net.stargraph.core.query;

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

import com.typesafe.config.Config;
import net.stargraph.core.query.nli.ClueAnalyzer;
import net.stargraph.query.InteractionMode;
import net.stargraph.query.Language;

import java.util.Objects;

import static net.stargraph.query.InteractionMode.NLI;

public final class InteractionModeSelector {
    private Config config;
    private Language language;

    public InteractionModeSelector(Config config, Language language) {
        this.config = Objects.requireNonNull(config);
        this.language = Objects.requireNonNull(language);
    }

    public InteractionMode detect(String queryString) {
        InteractionMode mode = NLI;

        if (queryString.contains("SELECT") || queryString.contains("ASK") || queryString.contains("CONSTRUCT")) {
            if (queryString.contains("PREFIX ") || queryString.contains("http:")) {
                mode = InteractionMode.SPARQL;
            } else {
                mode = InteractionMode.SA_SPARQL;
            }
        } else {
            if (queryString.contains("http:")) {
                mode = InteractionMode.SIMPLE_SPARQL;
            } else if (queryString.contains(":")) {
                mode = InteractionMode.SA_SIMPLE_SPARQL;
            }

            if (isEntitySimilarityQuery(queryString)) {
                mode = InteractionMode.ENTITY_SIMILARITY;
            }

            if (isDefinitionQuery(queryString)) {
                mode = InteractionMode.DEFINITION;
            }

            if (isClueQuery(queryString)) {
                mode = InteractionMode.CLUE;
            }
        }

        //TODO: other types will require configurable rules per language.

        return mode;
    }

    private boolean isEntitySimilarityQuery(String queryString){
        if (queryString == null || queryString.isEmpty()) {
            return false;
        }

        final String q = queryString.trim().toLowerCase();
        return q.contains("similar to") || q.contains("is like"); //TODO: What if this on the middle of the query?
    }

    private boolean isDefinitionQuery(String queryString){

        queryString = queryString.replace("Who is", "").replace("Who are", "").
                replace("What is", "").replace("What are", "").trim();

        queryString = queryString.replaceAll("\\.", "").replaceAll("\\?", "").replaceAll("\\!", "");
        for(String word : queryString.split(" ")) {
            if (!Character.isUpperCase(word.charAt(0)))
                return false;
        }

        if(queryString.contains("like"))
            return false;

        return true;
    }

    private boolean isClueQuery(String queryString){

        ClueAnalyzer clueAnalyzer = new ClueAnalyzer();
        return clueAnalyzer.isClue(queryString);
    }

}
