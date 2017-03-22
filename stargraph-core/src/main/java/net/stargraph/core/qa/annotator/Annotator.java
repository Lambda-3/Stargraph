package net.stargraph.core.qa.annotator;

import net.stargraph.Language;
import net.stargraph.StarGraphException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.List;

public abstract class Annotator {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected Marker marker = MarkerFactory.getMarker("annotator");

    protected abstract List<Word> doRun(Language language, String sentence);

    public final List<Word> run(Language language, String sentence) {
        logger.info(marker, "Annotating '{}', language: '{}'", sentence, language);
        try {
            return doRun(language, sentence);
        }
        catch (Exception e) {
            logger.error(marker, "Erro caught during annotation of '{}' ({})", sentence, language);
            throw new StarGraphException(e);
        }
    }
}
