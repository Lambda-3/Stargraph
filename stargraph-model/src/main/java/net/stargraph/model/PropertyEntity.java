package net.stargraph.model;

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

import net.stargraph.data.processor.Hashable;
import net.stargraph.model.wordnet.WNTuple;
import net.stargraph.rank.Rankable;

import java.util.Collection;
import java.util.Objects;

public final class PropertyEntity implements Hashable, Rankable {
    private String id;
    private String value;
    private Collection<WNTuple> hypernyms;
    private Collection<WNTuple> hyponyms;
    private Collection<WNTuple> synonyms;

    public PropertyEntity(String id, String value) {
        this(id, value, null, null, null);
    }

    public PropertyEntity(String id, String value,
                          Collection<WNTuple> hypernyms,
                          Collection<WNTuple> hyponyms,
                          Collection<WNTuple> synonyms) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("`id` is required");
        }

        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("`value` is required");
        }

        this.id = id;
        this.value = value;
        this.hypernyms = hypernyms;
        this.hyponyms = hyponyms;
        this.synonyms = synonyms;
    }

    public Collection<WNTuple> getHypernyms() {
        return hypernyms;
    }

    public Collection<WNTuple> getHyponyms() {
        return hyponyms;
    }

    public Collection<WNTuple> getSynonyms() {
        return synonyms;
    }

    public String getId() {
        return id;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PropertyEntity that = (PropertyEntity) o;

        return getId() != null ? getId().equals(that.getId()) : that.getId() == null;

    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PropertyEntity{" +
                "id='" + id + '\'' +
                '}';
    }
}
