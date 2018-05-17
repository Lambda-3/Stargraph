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
import net.stargraph.core.graph.JModel;
import net.stargraph.data.Indexable;
import net.stargraph.model.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Statement;

import static net.stargraph.ModelUtils.createInstance;
import static net.stargraph.ModelUtils.createProperty;

final class FactGraphIterator extends GraphIterator<Indexable> {

    public FactGraphIterator(Stargraph stargraph, KBId kbId, JModel model) {
        super(stargraph, kbId, model);
    }

    public FactGraphIterator(Stargraph stargraph, KBId kbId) {
        super(stargraph, kbId);
    }

    @Override
    protected Indexable buildNext(Statement statement) {
        InstanceEntity instanceEntity = createInstance(applyNS(statement.getSubject().getURI()));
        PropertyEntity propertyEntity = createProperty(applyNS(statement.getPredicate().getURI()));

        LabeledEntity labeledEntity;

        if (!statement.getObject().isLiteral()) {
            //Is created as an instance but can be changed to a class down on the workflow in EntityClassifierProcessor.
            labeledEntity = createInstance(applyNS(statement.getObject().asResource().getURI()));
        } else {
            Literal literal = statement.getObject().asLiteral();
            String dataType = literal.getDatatypeURI();
            String langTag = literal.getLanguage();
            String value = literal.getLexicalForm();
            labeledEntity = new ValueEntity(value, dataType, langTag);
        }


        return new Indexable(new Fact(kbId, instanceEntity, propertyEntity, labeledEntity), kbId);
    }
}
