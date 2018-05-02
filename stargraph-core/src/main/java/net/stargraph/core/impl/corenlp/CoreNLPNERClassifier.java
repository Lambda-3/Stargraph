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

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import net.stargraph.StarGraphException;
import net.stargraph.query.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Wrapper class for the classifier of CoreNLP that does Named Entity Recognition.
 */
public class CoreNLPNERClassifier {
    private final static Logger LOGGER = LoggerFactory.getLogger(CoreNLPNERClassifier.class);

    private static AbstractSequenceClassifier<CoreLabel> classifier = null;

    public CoreNLPNERClassifier(Language language) {
        if (classifier == null) {
            classifier = initCoreNLPNERClassifier(language);
        }
    }

    private static AbstractSequenceClassifier<CoreLabel> initCoreNLPNERClassifier(Language language) {

        AbstractSequenceClassifier<CoreLabel> classifier;

        switch (language) {
            case EN:
                try {
                    classifier = CRFClassifier.getClassifier("edu/stanford/nlp/models/ner/english.muc.7class.distsim.crf.ser.gz");
                } catch (IOException | ClassNotFoundException e) {
                    LOGGER.error("Cannot initialize english CoreNLP model: {}", e.getMessage());
                    throw new StarGraphException("CoreNLP models not found; cannot continue");
                }
                break;
            case DE:
                try {
                    // classifier = CRFClassifier.getClassifier("edu/stanford/nlp/models/ner/german.hgc_175m_600.crf.ser.gz");
                    classifier = CRFClassifier.getClassifier("edu/stanford/nlp/models/ner/german.dewac_175m_600.crf.ser.gz");
                } catch (IOException | ClassNotFoundException e) {
                    LOGGER.error("Cannot initialize german CoreNLP model: {}", e.getMessage());
                    throw new StarGraphException("CoreNLP models not found; cannot continue");
                }
                break;
            default:
                throw new UnsupportedOperationException("This language is not supported.");
        }

        return classifier;
    }

    public List<List<CoreLabel>> classify(String text) {
        return classifier.classify(text);
    }
}
