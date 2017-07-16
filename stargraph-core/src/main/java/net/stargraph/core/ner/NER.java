package net.stargraph.core.ner;

import java.util.List;

/**
 * Named Entity Recognition & Linking Interface
 */
public interface NER {

    List<LinkedNamedEntity> searchAndLink(String text);
}
