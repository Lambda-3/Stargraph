package net.stargraph.data;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class DataSource<T> {
    public abstract Iterator<T> createIterator();

    public Stream<T> getStream() {
        return StreamSupport.stream(this.getSpliterator(), false);
    }

    public Spliterator<T> getSpliterator() {
        return Spliterators.spliteratorUnknownSize(createIterator(), Spliterator.NONNULL);
    }
}
