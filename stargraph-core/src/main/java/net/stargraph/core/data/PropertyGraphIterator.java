package net.stargraph.core.data;

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

import net.stargraph.core.Stargraph;
import net.stargraph.core.graph.BaseGraphModel;
import net.stargraph.data.Indexable;
import net.stargraph.model.KBId;
import net.stargraph.model.PropertyEntity;
import org.apache.jena.rdf.model.Statement;

import static net.stargraph.ModelCreator.createProperty;

final class PropertyGraphIterator extends GraphIterator<Indexable> {

    public PropertyGraphIterator(Stargraph stargraph, KBId kbId, BaseGraphModel model) {
        super(stargraph, kbId, model);
    }

    public PropertyGraphIterator(Stargraph stargraph, KBId kbId) {
        super(stargraph, kbId);
    }

    @Override
    protected Indexable buildNext(Statement statement) {
        PropertyEntity propertyEntity = createProperty(applyNS(statement.getPredicate().getURI()));
        return new Indexable(propertyEntity, kbId);
    }
}
