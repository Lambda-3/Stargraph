package net.stargraph.core.query.annotator;

import com.typesafe.config.Config;

import java.util.Objects;

public abstract class AnnotatorFactory {
    protected Config config;

    public AnnotatorFactory(Config config) {
        this.config = Objects.requireNonNull(config);
    }

    public abstract Annotator create();

}
