package net.stargraph.core.serializer;

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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.stargraph.core.processors.FactClassifierProcessor;
import net.stargraph.model.KBId;
import net.stargraph.model.PropertyEntity;

import java.io.IOException;

class PropertySerializer extends AbstractSerializer<PropertyEntity> {

    PropertySerializer(KBId kbId) {
        super(kbId, PropertyEntity.class);
    }

    @Override
    public void serialize(PropertyEntity value, JsonGenerator g, SerializerProvider provider) throws IOException {
        g.writeStartObject();
        g.writeStringField("id", value.getId());
        if (!FactClassifierProcessor.isRelation(value)) {
            g.writeStringField("value", value.getValue());

            if (value.getHypernyms() != null && !value.getHypernyms().isEmpty()) {
                g.writeObjectField("hypernyms", value.getHypernyms());
            }
            if (value.getHyponyms() != null && !value.getHyponyms().isEmpty()) {
                g.writeObjectField("hyponyms", value.getHyponyms());
            }
            if (value.getSynonyms() != null && !value.getSynonyms().isEmpty()) {
                g.writeObjectField("synonyms", value.getSynonyms());
            }
        }
        g.writeEndObject();
    }
}
