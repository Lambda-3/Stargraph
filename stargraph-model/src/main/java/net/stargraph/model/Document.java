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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A Document.
 */
public final class Document implements Hashable {
    private String id;
    private String title;
    private String summary;
    private String text;
    private List<Passage> passages;

    public Document(String id, String title, String text) {
        this(id, title, null, text, null);
    }

    public Document(String id, String title, String summary, String text) {
        this(id, title, summary, text, null);
    }

    public Document(String id, String title, String summary, String text, List<Passage> passages) {
        this.id = Objects.requireNonNull(id);
        this.title = Objects.requireNonNull(title);
        this.text = Objects.requireNonNull(text);
        this.summary = summary;
        this.passages = (passages != null) ? passages : new ArrayList<>() ;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getText() {
        return text;
    }

    public List<Passage> getPassages() {
        return passages;
    }

    @Override
    public String toString() {
        String abbrevSummary = (summary == null) ? "NULL" : (summary.length() > 30)? text.substring(0, 30-3) + "..." : text;
        String abbrevText = (text.length() > 30)? text.substring(0, 30-3) + "..." : text;
        return "Document{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", summary='" + abbrevSummary + '\'' +
                ", text='" + abbrevText + '\'' +
                ", passages=" + passages +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return id.equals(document.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
