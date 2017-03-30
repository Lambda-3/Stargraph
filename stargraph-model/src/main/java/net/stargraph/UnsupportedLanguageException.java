package net.stargraph;

import net.stargraph.query.Language;

public final class UnsupportedLanguageException extends StarGraphException {
    public UnsupportedLanguageException(Language language) {
        super("Unsupported language: '" + language + "'");
    }
}
