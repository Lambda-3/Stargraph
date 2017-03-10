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
import net.stargraph.StarGraphException;
import net.stargraph.core.WordNet;
import net.stargraph.data.processor.BaseProcessor;
import net.stargraph.data.processor.Holder;
import net.stargraph.data.processor.ProcessorException;
import net.stargraph.model.PropertyEntity;
import net.stargraph.model.wordnet.WNTuple;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Collection;

public final class WordNetProcessor extends BaseProcessor {
    public static String name = "wordnet";
    private WordNet wordNet;

    public WordNetProcessor(Config config) {
        super(config);
        try {
            File wnFile = new File(getConfig().getString("wordnet-dir"));
            this.wordNet = new WordNet(wnFile);
        } catch (FileNotFoundException e) {
            throw new StarGraphException(e);
        }
    }

    @Override
    public void doRun(Holder<Serializable> holder) throws ProcessorException {
        PropertyEntity propertyEntity = (PropertyEntity) holder.get();
        String propValue = propertyEntity.getValue();
        Collection<WNTuple> hypernyms = wordNet.getHypernyms(propValue);
        Collection<WNTuple> hyponyms = wordNet.getHyponyms(propValue);
        Collection<WNTuple> synonyms = wordNet.getSynonyms(propValue);
        PropertyEntity richProp = new PropertyEntity(propertyEntity.getId(), propValue, hypernyms, hyponyms, synonyms);
        holder.set(richProp);
    }

    @Override
    public String getName() {
        return name;
    }
}
