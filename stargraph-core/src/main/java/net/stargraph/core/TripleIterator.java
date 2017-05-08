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

import net.stargraph.model.KBId;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

abstract class TripleIterator<T> implements Iterator<T> {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected Marker marker = MarkerFactory.getMarker("core");
    protected KBId kbId;
    protected Model model;

    private StmtIterator innerIt;
    private Statement currentStmt;
    private Namespace namespace;

    TripleIterator(Stargraph core, KBId kbId) {
        this.model = core.getGraphModel(kbId.getId());
        this.namespace = Namespace.create(core, kbId.getId());
        this.kbId = Objects.requireNonNull(kbId);
        this.innerIt = Objects.requireNonNull(model).listStatements();
    }

    @Override
    public final boolean hasNext() {
        if (currentStmt != null) {
            return true;
        }

        while (innerIt.hasNext()) {
            currentStmt = innerIt.next();
            //skipping blank nodes.
            if ((!currentStmt.getSubject().isAnon() && !currentStmt.getObject().isAnon())) {
                return true;
            }
        }

        currentStmt = null;
        return false;
    }

    @Override
    public final T next() {
        try {
            if (currentStmt == null) {
                throw new NoSuchElementException();
            }
            return buildNext(currentStmt);
        } catch (Exception e) {
            logger.error(marker, "Error parsing: {}", currentStmt);
            throw e;
        } finally {
            currentStmt = null;
        }
    }

    protected abstract T buildNext(Statement statement);

    String applyNS(String uri) {
        if (namespace != null) {
            return namespace.shrinkURI(uri);
        }
        return uri;
    }
}
