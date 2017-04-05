package net.stargraph.core.impl.corenlp;

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

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import net.stargraph.UnsupportedLanguageException;
import net.stargraph.core.query.annotator.Annotator;
import net.stargraph.core.query.annotator.PartOfSpeechSet;
import net.stargraph.core.query.annotator.Word;
import net.stargraph.query.Language;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.stargraph.query.Language.EN;

public final class CoreNLPAnnotator extends Annotator {

    private Map<Language, MaxentTagger> taggers;

    public CoreNLPAnnotator() {
        taggers = new ConcurrentHashMap<>();
    }


    @Override
    protected List<Word> doRun(Language language, String sentence) {
        MaxentTagger tagger = taggers.computeIfAbsent(language, lang -> {
            if (lang == EN) {
                return new MaxentTagger("edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
            }
            throw new UnsupportedLanguageException(lang);
        });

        PartOfSpeechSet partOfSpeechSet = PartOfSpeechSet.getPOSSet(language);
        List<Word> words = new ArrayList<>();

        List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(sentence));
        sentences.forEach(s -> {
            tagger.tagSentence(s).forEach(taggedWord ->
                    words.add(new Word(partOfSpeechSet.valueOf(taggedWord.tag()), taggedWord.value())));
        });

        return words;
    }

}
