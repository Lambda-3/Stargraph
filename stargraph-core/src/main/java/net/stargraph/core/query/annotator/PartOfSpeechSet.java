package net.stargraph.core.query.annotator;

import net.stargraph.Language;
import net.stargraph.StarGraphException;
import net.stargraph.UnsupportedLanguageException;

import java.util.HashSet;

public abstract class PartOfSpeechSet extends HashSet<POSTag> {

    public final POSTag valueOf(String tagName) {
        return this.stream().filter(pos -> pos.getTag().equals(tagName))
                .findFirst().orElseThrow(() -> new StarGraphException("No mapping for '" + tagName + "'"));
    }

    public static PartOfSpeechSet getPOSSet(Language language) {
        switch (language) {
            case EN:
                return EnglishPOSSet.getInstance();
            case DE:
            case PT:
                throw new UnsupportedLanguageException(language);
            default:
                throw new StarGraphException("Unknown Language: '" + language + "'");
        }
    }
}
