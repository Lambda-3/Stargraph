package net.stargraph.core;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import net.stargraph.StarGraphException;
import net.stargraph.core.graph.GraphModelProviderFactory;
import net.stargraph.core.graph.GraphSearcher;
import net.stargraph.core.graph.JModel;
import net.stargraph.core.impl.corenlp.NERSearcher;
import net.stargraph.core.impl.jena.JenaGraphSearcher;
import net.stargraph.core.index.Indexer;
import net.stargraph.core.ner.NER;
import net.stargraph.core.search.BaseSearcher;
import net.stargraph.core.search.SearchQueryGenerator;
import net.stargraph.core.search.Searcher;
import net.stargraph.data.DataProvider;
import net.stargraph.data.DataProviderFactory;
import net.stargraph.model.KBId;
import net.stargraph.query.Language;
import net.stargraph.rank.ModifiableIndraParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * An instance of a Knowledge Base and its inner core components. What else could be?
 */
public final class KBCore {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Marker marker;

    private String kbName;
    private Config mainConfig;
    private Config kbConfig;
    private Language language;
    private String nerKbName;
    private JModel graphModel;
    private KBLoader kbLoader;
    private Namespace namespace;
    private Stargraph stargraph;
    private NER ner;
    private Map<String, Indexer> indexers;
    private Map<String, Searcher> searchers;
    private Map<String, SearchQueryGenerator> searchQueryGenerators;
    private boolean running;

    public KBCore(String kbName, Stargraph stargraph, boolean start) {
        this.kbName = Objects.requireNonNull(kbName);
        this.stargraph = Objects.requireNonNull(stargraph);
        this.mainConfig = stargraph.getMainConfig();
        this.kbConfig = mainConfig.getConfig(String.format("kb.%s", kbName));
        this.marker = MarkerFactory.getMarker(kbName);
        this.indexers = new ConcurrentHashMap<>();
        this.searchers = new ConcurrentHashMap<>();
        this.searchQueryGenerators = new ConcurrentHashMap<>();
        this.language = Language.valueOf(kbConfig.getString("language").toUpperCase());

        if (kbConfig.hasPathOrNull("ner-kb")) {
            if (kbConfig.getIsNull("ner-kb")) {
                throw new StarGraphException("No NER-KB configured.");
            }
            this.nerKbName = kbConfig.getString("ner-kb");
        } else {
            this.nerKbName = kbName;
        }

        this.namespace = Namespace.create(kbConfig);

        if (start) {
            initialize();
        }
    }

    public synchronized void initialize() {
        if (running) {
            throw new IllegalStateException("Already started.");
        }

        final List<String> modelNames = getKBIds().stream().map(KBId::getModel).collect(Collectors.toList());

        for (String modelId : modelNames) {
            final KBId kbId = KBId.of(kbName, modelId);
            logger.info(marker, "Initializing '{}'", kbId);
            IndicesFactory factory = stargraph.getIndicesFactory(kbId);

            Indexer indexer = factory.createIndexer(kbId, stargraph);

            if (indexer != null) {
                indexer.start();
                indexers.put(modelId, indexer);
            } else {
                logger.warn(marker, "No indexer created for {}", kbId);
            }

            BaseSearcher searcher = factory.createSearcher(kbId, stargraph);

            if (searcher != null) {
                searcher.start();
                searchers.put(modelId, searcher);
            } else {
                logger.warn(marker, "No searcher created for {}", kbId);
            }

            SearchQueryGenerator searchQueryGenerator = factory.createSearchQueryGenerator(kbId, stargraph);
            if (searchQueryGenerator != null) {
                searchQueryGenerators.put(modelId, searchQueryGenerator);
            } else {
                logger.warn(marker, "No search query generator created for {}", kbId);
            }
        }

        this.ner = new NERSearcher(language, stargraph.getEntitySearcher(), nerKbName);
        this.kbLoader = new KBLoader(this);
        this.running = true;
    }


    public synchronized void terminate() {
        if (!running) {
            throw new IllegalStateException("Already stopped.");
        }
        logger.info(marker, "Terminating '{}'", kbName);

        indexers.values().forEach(Indexer::stop);
        searchers.values().forEach(Searcher::stop);

        this.running = false;
    }

    public Config getConfig() {
        return kbConfig;
    }

    public Config getConfig(String path) {
        return kbConfig.getConfig(path);
    }

    public String getKBName() {
        return kbName;
    }

    public String getNERKBName() {
        return nerKbName;
    }

    public Language getLanguage() {
        return language;
    }

    public List<KBId> getKBIds() {
        ConfigObject typeObj = this.mainConfig.getObject(String.format("kb.%s.model", kbName));
        return typeObj.keySet().stream().map(modelName -> KBId.of(kbName, modelName)).collect(Collectors.toList());
    }

    public JModel getGraphModel() {
        checkRunning();
        if (graphModel == null) {
            GraphModelProviderFactory factory = stargraph.getGraphModelProviderFactory(kbName);
            logger.info(marker, "Create graph model for '{}' using '{}'", kbName, factory.getClass().getSimpleName());
            graphModel = factory.create(kbName).getGraphModel();
            if (graphModel == null) {
                throw new StarGraphException("Could not create graph model for: " + kbName);
            }
        }
        return graphModel;
    }

    public DataProvider getDataProvider(String modelId) {
        checkRunning();
        DataProviderFactory factory = stargraph.getDataProviderFactory(KBId.of(kbName, modelId));
        logger.info(marker, "Create data provider for '{}' using '{}'", KBId.of(kbName, modelId), factory.getClass().getSimpleName());
        return factory.create(KBId.of(kbName, modelId));
    }

    public Indexer getIndexer(String modelId) {
        checkRunning();
        if (indexers.containsKey(modelId)) {
            return indexers.get(modelId);
        }
        throw new StarGraphException("Indexer not found nor initialized: " + KBId.of(kbName, modelId));
    }

    public Searcher getSearcher(String modelId) {
        checkRunning();
        if (searchers.containsKey(modelId)) {
            return searchers.get(modelId);
        }
        throw new StarGraphException("Searcher not found nor initialized: " + KBId.of(kbName, modelId));
    }

    public SearchQueryGenerator getSearchQueryGenerator(String modelId) {
        if (searchQueryGenerators.containsKey(modelId)) {
            return searchQueryGenerators.get(modelId);
        }
        throw new StarGraphException("SearchQueryGenerator not found nor initialized: " + KBId.of(kbName, modelId));
    }

    public KBLoader getLoader() {
        checkRunning();
        return kbLoader;
    }

    public NER getNER() {
        return ner;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public GraphSearcher createGraphSearcher() {
        return new JenaGraphSearcher(kbName, stargraph);
    }

    public void configureDistributionalParams(ModifiableIndraParams params) {
        String indraUrl = stargraph.getMainConfig().getString("distributional-service.rest-url");
        String indraCorpus = stargraph.getMainConfig().getString("distributional-service.corpus");
        params.url(indraUrl).corpus(indraCorpus).language(language.code);
    }

    private void checkRunning() {
        if (!running) {
            throw new IllegalStateException("KB Core not started.");
        }
    }
}
