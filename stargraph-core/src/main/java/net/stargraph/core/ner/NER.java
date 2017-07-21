package net.stargraph.core.ner;

import java.util.List;

/**
 * Named Entity Recognition & Linking Interface
 */
public interface NER {

    /**
     * Recognizes and try to link with known entities.
     *
     * @param text The text to be analyzed.
     * @return List of Named Entities recognized and possibly linked/resolved.
     */
    List<LinkedNamedEntity> searchAndLink(String text);
}
