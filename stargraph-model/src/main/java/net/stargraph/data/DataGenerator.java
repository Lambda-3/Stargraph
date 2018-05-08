package net.stargraph.data;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class DataGenerator<X, T> {
    //public abstract Class<X> getInputClazz();
    public abstract Iterator<T> getIterator(X data);

    public Stream<T> getStream(X data) {
        return StreamSupport.stream(this.getSpliterator(data), false);
    }

    public Spliterator<T> getSpliterator(X data) {
        return Spliterators.spliteratorUnknownSize(getIterator(data), Spliterator.NONNULL);
    }
}
