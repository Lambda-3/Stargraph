package net.stargraph.core.query;

import net.stargraph.StarGraphException;
import net.stargraph.core.query.nli.DataModelBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TriplePattern {
    private String pattern;


    public TriplePattern(String pattern) {
        this.pattern = Objects.requireNonNull(pattern);
    }

    public String getPattern() {
        return pattern;
    }

    public List<DataModelBinding> map(List<DataModelBinding> bindingList) {
        List<DataModelBinding> mapped = new ArrayList<>();

        for (String s : pattern.split("\\s")) {
            if (!s.startsWith("?VAR") && !s.startsWith("TYPE")) {
                mapped.add(bindingList.stream()
                        .filter(b -> b.getPlaceHolder().equals(s))
                        .findAny().orElseThrow(() -> new StarGraphException("Unmapped placeholder '" + s + "'")));
            }
        }

        return mapped;
    }

    @Override
    public String toString() {
        return "Triple{" +
                "pattern='" + pattern + '\'' +
                '}';
    }
}
