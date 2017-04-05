package net.stargraph.core.query;

import com.typesafe.config.Config;
import net.stargraph.query.InteractionMode;
import net.stargraph.query.Language;

import java.util.Objects;

import static net.stargraph.query.InteractionMode.NLI;

public final class InterationModeSelector {
    private Config config;
    private Language language;

    public InterationModeSelector(Config config, Language language) {
        this.config = Objects.requireNonNull(config);
        this.language = Objects.requireNonNull(language);
    }

    public InteractionMode detect(String queryString) {
        InteractionMode mode = NLI;

        if (queryString.contains("SELECT") || queryString.contains("ASK") || queryString.contains("CONSTRUCT")) {
            if (queryString.contains("PREFIX ") || queryString.contains("http:")) {
                mode = InteractionMode.SPARQL;
            } else {
                mode = InteractionMode.SA_SPARQL;
            }
        } else {
            if (queryString.contains("http:")) {
                mode = InteractionMode.SIMPLE_SPARQL;
            } else if (queryString.contains(":")) {
                mode = InteractionMode.SA_SIMPLE_SPARQL;
            }
        }

        //TODO: other types will require configurable rules per language.

        return mode;
    }

}
