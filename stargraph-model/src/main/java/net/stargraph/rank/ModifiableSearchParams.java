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

import net.stargraph.model.BuiltInModel;
import net.stargraph.model.KBId;

public final class ModifiableSearchParams {

    private int limit;
    private String kbId;
    private String modelId;
    private String searchTerm;

    private ModifiableSearchParams(String kbId) {
        // defaults
        this.limit = -1;
        this.kbId = kbId;
    }

    public final ModifiableSearchParams term(String searchTerm) {
        this.searchTerm = searchTerm;
        return this;
    }

    public final ModifiableSearchParams limit(int maxEntries) {
        this.limit = maxEntries;
        return this;
    }

    public final ModifiableSearchParams model(String id) {
        this.modelId = id;
        return this;
    }

    public final ModifiableSearchParams model(BuiltInModel builtInModel) {
        this.modelId = builtInModel.modelId;
        return this;
    }


    public final int getLimit() {
        return limit;
    }

    public final KBId getKbId() {
        return KBId.of(kbId, modelId);
    }

    public final String getSearchTerm() {
        return searchTerm;
    }

    public static ModifiableSearchParams create(String dbId) {
        return new ModifiableSearchParams(dbId);
    }
}
