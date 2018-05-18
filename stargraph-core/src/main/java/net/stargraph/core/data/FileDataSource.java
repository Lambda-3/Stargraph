package net.stargraph.core.data;

import net.stargraph.StarGraphException;
import net.stargraph.core.Stargraph;
import net.stargraph.data.DataSource;
import net.stargraph.model.KBId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.*;
import java.util.Iterator;
import java.util.Objects;

public abstract class FileDataSource extends DataSource {
    private class EmptyIterator implements Iterator {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            return null;
        }
    }

    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected Marker marker = MarkerFactory.getMarker("core");

    protected final Stargraph stargraph;
    protected final KBId kbId;
    private final String storeFilename; // optional
    private final String resource;
    private final boolean required;

    public FileDataSource(Stargraph stargraph, KBId kbId, String resource, String storeFilename, boolean required) {
        this.stargraph = Objects.requireNonNull(stargraph);
        this.kbId = Objects.requireNonNull(kbId);
        this.resource = Objects.requireNonNull(resource);
        this.storeFilename = storeFilename;
        this.required = required;
    }

    protected abstract Iterator createIterator(File file);

    @Override
    public Iterator createIterator() {
        try {
            File file = DataUtils.getData(stargraph, resource, kbId, storeFilename).toFile();
            if (!file.exists()) {
                if (required) {
                    throw new FileNotFoundException("File not found: '" + file + "'");
                } else {
                    logger.info("File not found: '" + file + "'");
                    return new EmptyIterator();
                }
            } else {
                return createIterator(file);
            }
        } catch (Exception e) {
            throw new StarGraphException(e);
        }
    }
}
