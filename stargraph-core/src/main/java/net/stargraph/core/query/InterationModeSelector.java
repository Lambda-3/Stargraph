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
import net.stargraph.query.InteractionMode;
import net.stargraph.query.Language;

import java.util.Objects;

import static net.stargraph.query.InteractionMode.NLI;

public final class InterationModeSelector {
    private Config config;
    private Language language;

    public InterationModeSelector(Config config, Language language) {
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
        }

        //TODO: other types will require configurable rules per language.

        return mode;
    }

}
