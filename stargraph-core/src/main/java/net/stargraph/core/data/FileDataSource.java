package net.stargraph.core.data;

import net.stargraph.StarGraphException;
import net.stargraph.core.Stargraph;
import net.stargraph.data.DataSource;
import net.stargraph.model.KBId;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private final Stargraph stargraph;
    private final KBId kbId;
    private final String storeFilename; // optional
    private final String resourcePath;
    private final boolean required;

    public FileDataSource(Stargraph stargraph, KBId kbId, String resourcePath) {
        this(stargraph, kbId, resourcePath, null, true);
    }

    public FileDataSource(Stargraph stargraph, KBId kbId, String resourcePath, boolean required) {
        this(stargraph, kbId, resourcePath, null, required);
    }

    public FileDataSource(Stargraph stargraph, KBId kbId, String resourcePath, String storeFilename) {
        this(stargraph, kbId, resourcePath, storeFilename, true);
    }

    public FileDataSource(Stargraph stargraph, KBId kbId, String resourcePath, String storeFilename, boolean required) {
        this.stargraph = Objects.requireNonNull(stargraph);
        this.kbId = Objects.requireNonNull(kbId);
        this.resourcePath = Objects.requireNonNull(resourcePath);
        this.storeFilename = storeFilename;
        this.required = required;
    }

    protected abstract Iterator getIterator(Stargraph stargraph, KBId kbId, File file);

    @Override
    public Iterator getIterator() {
        try {
            // get/download file
            File file = getFilePath().toFile();
            if (!file.exists()) {
                if (required) {
                    throw new FileNotFoundException("File not found: '" + file + "'");
                } else {
                    logger.info("File not found: '" + file + "'");
                    return new EmptyIterator();
                }
            } else {
                return getIterator(stargraph, kbId, file);
            }
        } catch (Exception e) {
            throw new StarGraphException(e);
        }
    }

    private Path getFilePath() throws IOException {
        String dataDir = stargraph.getDataRootDir();

        // web resource
        if (resourcePath.startsWith("http://")) {
            String stFilename = (storeFilename != null)? storeFilename : FilenameUtils.getName(resourcePath);
            Path storePath = Paths.get(dataDir, kbId.getId(), kbId.getModel(), stFilename);

            if (storePath.toFile().exists()) {
                // already downloaded
                return storePath;
            } else {
                // download
                download(resourcePath, storePath.toFile());
                return storePath;
            }
        } else
        // It's an absolute path to file
        if (Paths.get(resourcePath).isAbsolute()) {
            return Paths.get(resourcePath);
        }
        else
        // It's relative to the 'facts' dir
        {
            return Paths.get(dataDir, kbId.getId(), kbId.getModel(), resourcePath);
        }
    }

    private void download(String urlStr, File file) throws IOException {
        logger.info(marker, "Downloading from: '{}'", urlStr);

        Files.createDirectories(Objects.requireNonNull(file).toPath().getParent());
        URL url = new URL(urlStr);
        try (BufferedInputStream bis = new BufferedInputStream(url.openStream())) {
            try (FileOutputStream fis = new FileOutputStream(file)) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = bis.read(buffer, 0, 8192)) != -1) {
                    fis.write(buffer, 0, count);
                }
            }
        }
    }
}
