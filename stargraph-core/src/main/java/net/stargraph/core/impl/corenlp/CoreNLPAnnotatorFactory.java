package net.stargraph.core.impl.corenlp;

import com.typesafe.config.Config;
import net.stargraph.core.query.annotator.Annotator;
import net.stargraph.core.query.annotator.AnnotatorFactory;

public final class CoreNLPAnnotatorFactory extends AnnotatorFactory {

    public CoreNLPAnnotatorFactory(Config config) {
        super(config);
    }

    @Override
    public Annotator create() {
        return new CoreNLPAnnotator();
    }
}
