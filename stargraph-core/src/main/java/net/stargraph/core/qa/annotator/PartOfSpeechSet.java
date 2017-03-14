package net.stargraph.core.qa.annotator;

import net.stargraph.StarGraphException;
import net.stargraph.core.qa.Language;

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
                throw new StarGraphException("Not Implemented yet!");
        }
        throw new StarGraphException("NO Part Of Speech Set defined for '" + language + "'");
    }
}
