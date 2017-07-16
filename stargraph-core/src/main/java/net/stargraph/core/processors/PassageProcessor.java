package net.stargraph.core.processors;

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

import com.typesafe.config.Config;
import net.stargraph.core.Stargraph;
import net.stargraph.core.ner.LinkedNamedEntity;
import net.stargraph.core.ner.NER;
import net.stargraph.data.processor.BaseProcessor;
import net.stargraph.data.processor.Holder;
import net.stargraph.data.processor.ProcessorException;
import net.stargraph.model.Document;
import net.stargraph.model.LabeledEntity;
import net.stargraph.model.Passage;
import org.lambda3.text.simplification.discourse.utils.sentences.SentencesUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Can be placed in the workflow to create passages.
 */
public final class PassageProcessor extends BaseProcessor {
    public static String name = "passage-processor";

    private Stargraph core;

    public PassageProcessor(Stargraph core, Config config) {
        super(config);
        this.core = Objects.requireNonNull(core);
    }

    @Override
    public void doRun(Holder<Serializable> holder) throws ProcessorException {
        Serializable entry = holder.get();
        NER ner = core.getNER(holder.getKBId().getId());

        if (entry instanceof Document) {
            Document document = (Document)entry;

            List<Passage> passages = new ArrayList<>();
            for (String sentence : SentencesUtils.splitIntoSentences(document.getText())) {
                List<LinkedNamedEntity> lners = ner.searchAndLink(sentence);
                List<LabeledEntity> entities = new ArrayList<>();

                // only add linked entities
                lners.stream().filter(l -> l.getEntity() != null).forEach(l -> entities.add(l.getEntity()));

                passages.add(new Passage(sentence, entities));
            }

            holder.set(new Document(document.getTitle(), document.getText(), passages));
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
