package net.stargraph.core.impl.corenlp;

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
