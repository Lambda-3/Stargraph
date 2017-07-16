package net.stargraph.core;

/*-
 * ==========================License-Start=============================
 * stargraph-core
 * --------------------------------------------------------------------
 * Copyright (C) 2017 Lambda^3
 * --------------------------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ==========================License-End===============================
 */

import com.typesafe.config.*;
import net.stargraph.ModelUtils;
import net.stargraph.StarGraphException;
import net.stargraph.core.graph.GraphSearcher;
import net.stargraph.core.impl.corenlp.NERSearcher;
import net.stargraph.core.impl.elastic.ElasticEntitySearcher;
import net.stargraph.core.impl.elastic.ElasticPassageSearcher;
import net.stargraph.core.impl.elastic.ElasticSearcher;
import net.stargraph.core.impl.hdt.HDTModelFactory;
import net.stargraph.core.impl.jena.JenaGraphSearcher;
import net.stargraph.core.index.Indexer;
import net.stargraph.core.index.IndexerFactory;
import net.stargraph.core.ner.NER;
import net.stargraph.core.processors.Processors;
import net.stargraph.core.search.BaseSearcher;
import net.stargraph.core.search.EntitySearcher;
import net.stargraph.core.search.PassageSearcher;
import net.stargraph.core.search.Searcher;
import net.stargraph.data.DataProvider;
import net.stargraph.data.DataProviderFactory;
import net.stargraph.data.DataQueue;
import net.stargraph.data.DataQueueFactory;
import net.stargraph.data.processor.Holder;
import net.stargraph.data.processor.Processor;
import net.stargraph.data.processor.ProcessorChain;
import net.stargraph.model.BuiltInModel;
import net.stargraph.model.KBId;
import net.stargraph.query.Language;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * The Stargraph database core implementation.
 */
public final class Stargraph {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private Marker marker = MarkerFactory.getMarker("core");
    private Config mainConfig;
    private Map<String, KBLoader> kbLoaders;
    private Map<KBId, DataQueue> dataQueues;
    private Map<KBId, Indexer> indexers;
    private Map<KBId, Searcher> searchers;
    private Map<String, Namespace> namespaces;
    private IndexerFactory indexerFactory;
    private GraphModelFactory modelFactory;
    private boolean initialized;

    public Stargraph() {
        this(ConfigFactory.load().getConfig("stargraph"), true);
    }

    public Stargraph(Config cfg, boolean initialize) {
        if (System.getProperty("config.file") == null) {
            logger.warn(marker, "No configuration found at '-Dconfig.file'.");
        }

        this.mainConfig = Objects.requireNonNull(cfg);
        logger.trace(marker, "Configuration: {}", ModelUtils.toStr(mainConfig));
        this.dataQueues = new ConcurrentHashMap<>();
        this.indexers = new ConcurrentHashMap<>();
        this.searchers = new ConcurrentHashMap<>();
        this.namespaces = new ConcurrentHashMap<>();
        this.kbLoaders = new ConcurrentHashMap<>();

        setIndexerFactory(createIndexerFactory());
        setModelFactory(new HDTModelFactory(this));

        if (initialize) {
            initialize();
        }
    }

    @SuppressWarnings("unchecked")
    public Class<Serializable> getModelClass(String modelName) {
        // This should change to support user's models.

        for (BuiltInModel entry : BuiltInModel.values()) {
            if (entry.modelId.equals(modelName)) {
                return entry.cls;
            }
        }

        throw new StarGraphException("No Class registered for model: '" + modelName + "'");
    }

    public Namespace getNamespace(String dbId) {
        return namespaces.computeIfAbsent(dbId, (id) -> Namespace.create(this, dbId));
    }

    public EntitySearcher createEntitySearcher() {
        return new ElasticEntitySearcher(this);
    }

    public PassageSearcher createPassageSearcher() {
        return new ElasticPassageSearcher(this);
    }

    public GraphSearcher createGraphSearcher(String dbId) {
        return new JenaGraphSearcher(dbId, this);
    }

    public Model getGraphModel(String dbId) {
        return modelFactory.getModel(dbId);
    }

    public Config getConfig() {
        return mainConfig;
    }

    public Config getKBConfig(String dbId) {
        return mainConfig.getConfig(String.format("kb.%s", dbId));
    }

    public Config getKBConfig(KBId kbId) {
        return mainConfig.getConfig(kbId.getKBPath());
    }

    public Config getTypeConfig(KBId kbId) {
        return mainConfig.getConfig(kbId.getTypePath());
    }

    public KBLoader getKBLoader(String dbId) {
        return kbLoaders.computeIfAbsent(dbId, (id) -> new KBLoader(this, id));
    }

    public List<KBId> getKBIdsOf(String dbId) {
        return searchers.keySet().stream()
                .filter(kbId -> kbId.getId().equals(Objects.requireNonNull(dbId))).collect(Collectors.toList());
    }

    public Set<KBId> getKBs() {
        return indexers.keySet();
    }

    public boolean hasKB(String id) {
        return getKBs().stream().anyMatch(kbId -> kbId.getId().equals(id));
    }

    public Language getLanguage(String dbId) {
        Config kbCfg = getKBConfig(dbId);
        return Language.valueOf(kbCfg.getString("language").toUpperCase());
    }

    public DataQueue getDataQueue(KBId kbId) {
        if (dataQueues.keySet().contains(kbId))
            return dataQueues.get(kbId);
        throw new StarGraphException("DataQueue not found nor initialized: " + kbId);
    }

    public NER getNER(String dbId) {
        //TODO: Should have a factory to ease test other implementation just changing configuration. See IndexerFactory.
        return new NERSearcher(getLanguage(dbId), createEntitySearcher(), dbId);
    }

    public Indexer getIndexer(KBId kbId) {
        if (indexers.keySet().contains(kbId))
            return indexers.get(kbId);
        throw new StarGraphException("Indexer not found nor initialized: " + kbId);
    }

    public Searcher getSearcher(KBId kbId) {
        if (searchers.keySet().contains(kbId))
            return searchers.get(kbId);
        throw new StarGraphException("Searcher not found nor initialized: " + kbId);
    }

    public void setIndexerFactory(IndexerFactory indexerFactory) {
        this.indexerFactory = Objects.requireNonNull(indexerFactory);
    }

    public void setModelFactory(GraphModelFactory modelFactory) {
        this.modelFactory = Objects.requireNonNull(modelFactory);
    }

    public ProcessorChain createProcessorChain(KBId kbId) {
        List<? extends Config> processorsCfg = getProcessorsCfg(kbId);
        if (processorsCfg != null && processorsCfg.size() != 0) {
            List<Processor> processors = new ArrayList<>();
            processorsCfg.forEach(config -> processors.add(Processors.create(this, config)));
            ProcessorChain chain = new ProcessorChain(processors);
            logger.info(marker, "processors = {}", chain);
            return chain;
        }
        return null;
    }

    public DataProvider<? extends Holder> createDataProvider(KBId kbId) {
        DataProviderFactory factory;

        try {
            String className = getDataProviderCfg(kbId).getString("class");
            Class<?> providerClazz = Class.forName(className);
            Constructor[] constructors = providerClazz.getConstructors();

            if (BaseDataProviderFactory.class.isAssignableFrom(providerClazz)) {
                // It's our internal factory hence we inject the core dependency.
                factory = (DataProviderFactory) constructors[0].newInstance(this);
            } else {
                // This should be a user factory without constructor.
                // API user should rely on configuration or other means to initialize.
                // See TestDataProviderFactory as an example
                factory = (DataProviderFactory) providerClazz.newInstance();
            }

            DataProvider<? extends Holder> provider = factory.create(kbId);

            if (provider == null) {
                throw new IllegalStateException("DataProvider not created!");
            }

            logger.info(marker, "Creating {} data provider", kbId);
            return provider;
        } catch (Exception e) {
            throw new StarGraphException("Fail to initialize data provider: " + kbId, e);
        }
    }

    public Optional<DataQueue<? extends Holder>> createDataQueue(KBId kbId) {
        if (!getDataQueueCfg(kbId).isPresent()) {
            return Optional.empty();
        }

        try {
            String className = getDataQueueCfg(kbId).get().getString("class");
            Class<?> clazz = Class.forName(className);
            Constructor[] constructors = clazz.getConstructors();

            DataQueueFactory factory;
            if (BaseDataQueueFactory.class.isAssignableFrom(clazz)) {
                // It's our internal factory hence we inject the core dependency.
                factory = (DataQueueFactory) constructors[0].newInstance(this);
            } else {
                // This should be a user factory without constructor.
                // API user should rely on configuration or other means to initialize.
                // See TestDataProviderFactory as an example
                factory = (DataQueueFactory) clazz.newInstance();
            }

            DataQueue<? extends Holder> dataQueue = factory.create(kbId);

            if (dataQueue == null) {
                throw new IllegalStateException("DataQueue not created!");
            }

            logger.info(marker, "Creating {} data queue", kbId);
            return Optional.of(dataQueue);
        } catch (Exception e) {
            throw new StarGraphException("Fail to initialize data queue: " + kbId, e);
        }
    }

    public synchronized final void initialize() {
        if (initialized) {
            throw new IllegalStateException("Core already initialized.");
        }

        this.initializeKB();
        logger.info(marker, "Indexer: '{}'", mainConfig.getString("indexer.factory.class"));
        logger.info(marker, "DS Service Endpoint: '{}'", mainConfig.getString("distributional-service.rest-url"));
        logger.info(marker, "★☆ {}, {} ({}) ★☆", Version.getCodeName(), Version.getBuildVersion(), Version.getBuildNumber());
        initialized = true;
    }

    public synchronized final void terminate() {
        if (!initialized) {
            throw new IllegalStateException("Not initialized");
        }
        initialized = false;
        // future shutdown procedure if needed.
    }

    private void initializeKB() {
        ConfigObject kbObj;
        try {
            kbObj = this.mainConfig.getObject("kb");
        } catch (ConfigException e) {
            throw new StarGraphException("No KB configured.", e);
        }

        for (Map.Entry<String, ConfigValue> kbEntry : kbObj.entrySet()) {
            final String kbName = kbEntry.getKey();
            Config kbCfg = this.mainConfig.getConfig(String.format("kb.%s", kbName));

            if (!kbCfg.getBoolean("enabled")) {
                logger.info(marker, "KB {} is disabled.", kbName);
            } else {
                ConfigObject typeObj = this.mainConfig.getObject(String.format("kb.%s.model", kbEntry.getKey()));
                for (Map.Entry<String, ConfigValue> typeEntry : typeObj.entrySet()) {
                    KBId kbId = KBId.of(kbName, typeEntry.getKey());
                    logger.info(marker, "Initializing {}", kbId);
                    Optional<DataQueue<? extends Holder>> dataQueue = createDataQueue(kbId);
                    if (dataQueue.isPresent()) {
                        dataQueues.put(kbId, dataQueue.get());
                    }
                    Indexer indexer = this.indexerFactory.create(kbId, this);
                    indexer.start();
                    indexers.put(kbId, indexer);
                    BaseSearcher searcher = new ElasticSearcher(kbId, this);
                    searcher.start();
                    searchers.put(kbId, searcher);
                }
            }
        }

        if (searchers.isEmpty()) {
            logger.warn(marker, "No KBs configured.");
        }
    }

    private List<? extends Config> getProcessorsCfg(KBId kbId) {
        String path = String.format("%s.processors", kbId.getTypePath());
        if (mainConfig.hasPath(path)) {
            return mainConfig.getConfigList(path);
        }
        return null;
    }

    private Config getDataProviderCfg(KBId kbId) {
        String path = String.format("%s.provider", kbId.getTypePath());
        return mainConfig.getConfig(path);
    }

    private Optional<Config> getDataQueueCfg(KBId kbId) {
        String path = String.format("%s.provider-queue", kbId.getTypePath());
        if (!mainConfig.hasPath(path)) {
            return Optional.empty();
        }
        return Optional.of(mainConfig.getConfig(path));
    }


    private IndexerFactory createIndexerFactory() {
        try {
            String className = getConfig().getString("indexer.factory.class");
            Class<?> providerClazz = Class.forName(className);
            Constructor<?> constructor = providerClazz.getConstructors()[0];
            return (IndexerFactory) constructor.newInstance();
        } catch (Exception e) {
            throw new StarGraphException("Can't initialize indexers.", e);
        }
    }

}
