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

public final class ParamsBuilder {

    public static ModifiableRankParams stringDistance() {
        return new ModifiableRankParams();
    }

    public static ModifiableRankParams levenshtein() {
        return new ModifiableRankParams(Threshold.auto(), RankingModel.LEVENSHTEIN);
    }

    public static ModifiableRankParams fuzzy() {
        return new ModifiableRankParams(Threshold.auto(), RankingModel.FUZZY);
    }

    public static ModifiableRankParams jaccard() {
        return new ModifiableRankParams(Threshold.auto(), RankingModel.JACCARD);
    }

    public static ModifiableRankParams jarowinkler() {
        return new ModifiableRankParams(Threshold.auto(), RankingModel.JAROWINKLER);
    }

    public static ModifiableIndraParams distributional() {
        return new ModifiableIndraParams();
    }

    public static ModifiableIndraParams word2vec() {
        return new ModifiableIndraParams(Threshold.auto(), RankingModel.W2V);
    }

    public static ModifiableIndraParams esa() {
        return new ModifiableIndraParams(Threshold.auto(), RankingModel.ESA);
    }

    public static ModifiableIndraParams lsa() {
        return new ModifiableIndraParams(Threshold.auto(), RankingModel.LSA);
    }

    public static ModifiableIndraParams glove() {
        return new ModifiableIndraParams(Threshold.auto(), RankingModel.GLOVE);
    }
}
