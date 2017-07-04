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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A Document.
 */
public final class Document implements Hashable, Serializable {
    private String title;
    private String text;
    private List<Passage> passages; // will be created during indexing time

    public Document(String title, String text) {
        this(title, text, new ArrayList<>());
    }

    public Document(String title, String text, List<Passage> passages) {
        if (title == null || text == null) {
            throw new IllegalArgumentException();
        }
        this.title = title;
        this.text = text;
        this.passages = passages;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public List<Passage> getPassages() {
        return passages;
    }

    @Override
    public String toString() {
        String abbrevText = (text.length() > 30)? text.substring(0, 30-3) + "..." : text;
        return "Document{" +
                "title='" + title + '\'' +
                ", text='" + abbrevText + '\'' +
                ", passages=" + passages +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document fact = (Document) o;
        return Objects.equals(title, fact.title) &&
                Objects.equals(text, fact.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, text);
    }
}
