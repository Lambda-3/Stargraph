package net.stargraph.core.query.annotator;

import java.util.Objects;

public final class Word {
    private POSTag posTag;
    private String text;

    public Word(POSTag posTag, String text) {
        this.posTag = posTag;
        this.text = text;
    }

    public POSTag getPosTag() {
        return posTag;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return text + "/" + posTag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word = (Word) o;
        return Objects.equals(posTag, word.posTag) &&
                Objects.equals(text, word.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(posTag, text);
    }
}
