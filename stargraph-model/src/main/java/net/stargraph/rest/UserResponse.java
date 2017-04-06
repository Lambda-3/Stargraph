package net.stargraph.rest;

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

import net.stargraph.query.InteractionMode;

import java.io.Serializable;
import java.util.Objects;

public abstract class UserResponse implements Serializable {
    private String query;
    private InteractionMode interactionMode;

    public UserResponse(String query, InteractionMode interactionMode) {
        this.query = Objects.requireNonNull(query);
        this.interactionMode = Objects.requireNonNull(interactionMode);
    }

    public final String getQuery() {
        return query;
    }

    public final InteractionMode getInteractionMode() {
        return interactionMode;
    }

    public static class EntityEntry {
        public String id;
        public String value;
        public double score;

        public EntityEntry(String id, String value) {
            this(id, value, 1);
        }

        public EntityEntry(String id, String value, double score) {
            this.id = id;
            this.value = value;
            this.score = score;
        }
    }
}
