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
import com.fasterxml.jackson.databind.ObjectMapper;
import net.stargraph.core.processors.ner.LinkedNamedEntity;
import net.stargraph.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class DocumentDeserializer extends AbstractDeserializer<Document> {

    DocumentDeserializer(KBId kbId) {
        super(kbId, PropertyEntity.class);
    }

    @Override
    public Document deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        String title = node.get("title").asText();
        String text = node.get("text").asText();

        List<Passage> passages = new ArrayList();
        if (node.has("passages")) {
            for (final JsonNode pass : node.get("passages")) {
                String passText = pass.get("text").asText();
                List<LabeledEntity> entities = new ArrayList<>();
                if (pass.has("entities")) {
                    for (final JsonNode ent : pass.get("entities")) {
                        String entId = ent.get("id").asText();
                        String entValue = ent.get("value").asText();

                        if (ent.has("complex")) {
                            entities.add(new ClassEntity(entId, entValue, ent.get("complex").asBoolean()));
                        } else if (ent.has("language")) {
                            entities.add(new ValueEntity(entId, entValue, ent.get("dataType").asText(null), ent.get("language").asText(null)));
                        } else {
                            entities.add(new InstanceEntity(entId, entValue));
                        }
                    }
                }
                passages.add(new Passage(passText, entities));
            }
        }

        return new Document(title, text, passages);
    }


}
