package net.stargraph.core.query;

import com.typesafe.config.Config;
import net.stargraph.core.Stargraph;

import java.util.Objects;

public final class QueryEngine {
    private Config config;
    private Stargraph core;
    private Analyzers analyzers;

    public QueryEngine(Config config) {
        this.config = Objects.requireNonNull(config);
        this.analyzers = new Analyzers(config);
        this.core = new Stargraph(config, true);
    }
}
