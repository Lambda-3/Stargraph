package net.stargraph.data;

import java.util.Iterator;

public interface DataSource<T> {
    Iterator<T> getIterator();
}
