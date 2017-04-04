package net.stargraph.rank;

/*-
 * ==========================License-Start=============================
 * stargraph-model
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

import org.lambda3.indra.client.ScoreFunction;

import java.util.Objects;

public final class ModifiableIndraParams extends ModifiableRankParams {
    private String url;
    private String corpus;
    private String language;
    private ScoreFunction scoreFunction;

    public ModifiableIndraParams(Threshold threshold, RankingModel rankingModel) {
        super(threshold, rankingModel);
        this.scoreFunction = ScoreFunction.COSINE;
    }

    public ModifiableIndraParams() {
        super();
    }

    public ModifiableIndraParams url(String serviceUrl) {
        this.url = Objects.requireNonNull(serviceUrl);
        return this;
    }

    public ModifiableIndraParams corpus(String corpus) {
        this.corpus = Objects.requireNonNull(corpus);
        return this;
    }

    public ModifiableIndraParams language(String languageCode) {
        this.language = Objects.requireNonNull(languageCode);
        return this;
    }

    public ModifiableIndraParams scoreFunction(ScoreFunction scoreFunction) {
        this.scoreFunction = Objects.requireNonNull(scoreFunction);
        return this;
    }

    public String getUrl() {
        return url;
    }

    public String getCorpus() {
        return corpus;
    }

    public String getLanguage() {
        return language;
    }

    public ScoreFunction getScoreFunction() {
        return scoreFunction;
    }

    @Override
    public String toString() {
        return "ModifiableIndraParams{" +
                "url='" + url + '\'' +
                ", corpus='" + corpus + '\'' +
                ", language='" + language + '\'' +
                ", scoreFunction='" + scoreFunction + '\'' +
                ", threshold=" + this.getThreshold() +
                '}';
    }
}
