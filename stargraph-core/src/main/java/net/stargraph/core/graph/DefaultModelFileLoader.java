package net.stargraph.core.graph;

import net.stargraph.StarGraphException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

public class DefaultModelFileLoader {
    protected static Logger logger = LoggerFactory.getLogger(DefaultModelFileLoader.class);
    protected static Marker marker = MarkerFactory.getMarker("core");

    protected final String dbId;
    protected final File file;
    protected final Lang lang; // optional

    public DefaultModelFileLoader(String dbid, File file) {
        this.dbId = dbid;
        this.file = file;
        this.lang = null;
    }

    public DefaultModelFileLoader(String dbid, File file, Lang lang) {
        this.dbId = dbid;
        this.file = file;
        this.lang = lang;
    }

    public JModel loadModel() {
        logger.info(marker, "Loading '{}'", file.getAbsolutePath());

        JModel model = null;

        try {
            Model m = ModelFactory.createDefaultModel();
            if (lang != null) {
                RDFDataMgr.read(m, file.getAbsolutePath(), lang);
            } else {
                RDFDataMgr.read(m, file.getAbsolutePath());
            }
            model = new JModel(m);
        } catch (Exception e) {
            throw new StarGraphException(e);
        } finally {
            if (model == null) {
                logger.error(marker, "No Graph Model instantiated for {}", dbId);
            }
        }

        return model;
    }

    public final Iterator<JModel> loadModelAsIterator() {
        return Arrays.asList(loadModel()).iterator();
    }
}
