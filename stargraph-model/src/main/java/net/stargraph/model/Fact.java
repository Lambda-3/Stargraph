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

import java.util.Objects;

/**
 * A Fact.
 */
public final class Fact implements Hashable {
    private KBId kbId;
    private ContextId subject;
    private PropertyEntity predicate;
    private LabeledEntity object;

    public Fact(KBId kbId, ContextId subject, PropertyEntity predicate, LabeledEntity object) {
        if (kbId == null || subject == null || predicate == null || object == null) {
            throw new IllegalArgumentException();
        }
        this.kbId = kbId;
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    public KBId getKBId() {
        return kbId;
    }

    public ContextId getSubject() {
        return subject;
    }

    public PropertyEntity getPredicate() {
        return predicate;
    }

    public LabeledEntity getObject() {
        return object;
    }

    @Override
    public String toString() {
        return "Fact{" +
                "s=" + subject +
                ",p=" + predicate +
                ",o=" + object +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fact fact = (Fact) o;
        return Objects.equals(kbId, fact.kbId) &&
                Objects.equals(subject, fact.subject) &&
                Objects.equals(predicate, fact.predicate) &&
                Objects.equals(object, fact.object);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kbId, subject, predicate, object);
    }
}
