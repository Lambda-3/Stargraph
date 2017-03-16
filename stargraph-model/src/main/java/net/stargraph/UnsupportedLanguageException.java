package net.stargraph;

public final class UnsupportedLanguageException extends StarGraphException {
    public UnsupportedLanguageException(Language language) {
        super("Unsupported language: '" + language + "'");
    }
}
