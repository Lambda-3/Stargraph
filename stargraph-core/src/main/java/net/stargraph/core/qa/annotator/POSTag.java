package net.stargraph.core.qa.annotator;

import java.util.Objects;

public class POSTag {
    private String tag;

    POSTag(String tag) {
        this.tag = Objects.requireNonNull(tag);
    }

    public final String getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        POSTag posTag = (POSTag) o;
        return Objects.equals(tag, posTag.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag);
    }

    @Override
    public String toString() {
        return tag;
    }
}
