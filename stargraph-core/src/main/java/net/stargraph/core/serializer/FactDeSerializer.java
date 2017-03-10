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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import net.stargraph.model.*;

import java.io.IOException;

class FactDeSerializer extends AbstractDeserializer<Fact> {

    FactDeSerializer(KBId kbId) {
        super(kbId, Fact.class);
    }

    @Override
    public Fact deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);

        JsonNode s = node.get("s");
        InstanceEntity subject = new InstanceEntity(s.get("id").asText(), s.get("value").asText());

        JsonNode p = node.get("p");
        String propId = p.get("id").asText();
        String propValue = p.has("value") ? p.get("value").asText() : propId;
        PropertyEntity property = new PropertyEntity(propId, propValue);

        JsonNode o = node.get("o");
        LabeledEntity object;
        String objId = o.get("id").asText();
        String objValue = o.get("value").asText();

        if (o.has("complex")) {
            object = new ClassEntity(objId, objValue, o.get("complex").asBoolean());
        } else if (o.has("language")) {
            object = new ValueEntity(objId, objValue, o.get("dataType").asText(null), o.get("language").asText(null));
        } else {
            object = new InstanceEntity(objId, objValue);
        }

        return new Fact(getKbId(), subject, property, object);
    }


}
