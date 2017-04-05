package net.stargraph.core.impl.opennlp;

import com.typesafe.config.Config;
import net.stargraph.core.query.annotator.Annotator;
import net.stargraph.core.query.annotator.AnnotatorFactory;

public final class OpenNLPAnnotatorFactory extends AnnotatorFactory {

    public OpenNLPAnnotatorFactory(Config config) {
        super(config);
    }

    @Override
    public Annotator create() {
        return new OpenNLPAnnotator(this.config);
    }

}
