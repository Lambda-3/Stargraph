package net.stargraph.core;

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

import net.stargraph.data.Indexable;
import net.stargraph.model.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Statement;

import javax.print.Doc;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static net.stargraph.ModelUtils.createInstance;
import static net.stargraph.ModelUtils.createProperty;

final class DocumentIterator implements Iterator<Indexable> {
    private KBId kbId;
    private Iterator<Document> innerIt;

    DocumentIterator(KBId kbId, List<Document> documents) {
        this.kbId = kbId;

        this.innerIt = documents.iterator();
    }

    @Override
    public boolean hasNext() {
        return innerIt.hasNext();
    }

    @Override
    public Indexable next() {
        Document document = innerIt.next();

        return new Indexable(document, kbId);
    }
}
