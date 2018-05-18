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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import net.stargraph.ModelUtils;
import net.stargraph.StarGraphException;
import net.stargraph.core.data.BaseDataProviderFactory;
import net.stargraph.core.graph.BaseGraphModelProviderFactory;
import net.stargraph.core.graph.DefaultGraphModelProviderFactory;
import net.stargraph.core.graph.GraphModelProviderFactory;
import net.stargraph.core.index.Indexer;
import net.stargraph.core.processors.Processors;
import net.stargraph.core.search.EntitySearcher;
import net.stargraph.core.search.Searcher;
import net.stargraph.data.DataProviderFactory;
import net.stargraph.data.processor.Processor;
import net.stargraph.data.processor.ProcessorChain;
import net.stargraph.model.KBId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * The Stargraph database core implementation.
 */
public final class Stargraph {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Marker marker = MarkerFactory.getMarker("stargraph");

    private Config mainConfig;
    private String dataRootDir;
    private IndicesFactory indicesFactory;
    private GraphModelProviderFactory defaultGraphModelProviderFactory;
    private Map<String, KBCore> kbCoreMap;
    private Set<String> kbInitSet;
    private EntitySearcher entitySearcher;
    private boolean initialized;

    /**
     * Constructs a new Stargraph core API entry-point.
     */
    public Stargraph() {
        this(ConfigFactory.load().getConfig("stargraph"), true);
    }

    /**
     * Constructs a new Stargraph core API entry-point.
     *
     * @param cfg Configuration instance.
     * @param initKBs Controls the startup behaviour. Use <code>false</code> to postpone KB specific initialization.
     */
    public Stargraph(Config cfg, boolean initKBs) {
        logger.info(marker, "Memory: {}", ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());
        this.mainConfig = Objects.requireNonNull(cfg);
        logger.trace(marker, "Configuration: {}", ModelUtils.toStr(mainConfig));
        // Only KBs in this set will be initialized. Unit tests appreciates!
        this.kbInitSet = new LinkedHashSet<>();
        this.kbCoreMap = new ConcurrentHashMap<>(8);

        this.entitySearcher = new EntitySearcher(this);

        // Configurable defaults
        setDataRootDir(mainConfig.getString("data.root-dir")); // absolute path is expected
        setDefaultIndicesFactory(createDefaultIndicesFactory());
        setDefaultGraphModelProviderFactory(new DefaultGraphModelProviderFactory(this));

        if (initKBs) {
            initialize();
        }
    }

    public KBCore getKBCore(String dbId) {
        if (kbCoreMap.containsKey(dbId)) {
            return kbCoreMap.get(dbId);
        }
        throw new StarGraphException("KB not found: '" + dbId + "'");
    }

    public Collection<KBCore> getKBs() {
        return kbCoreMap.values();
    }

    public boolean hasKB(String kbName) {
        return getKBs().stream().anyMatch(core -> core.getKBName().equals(kbName));
    }

    public Indexer getIndexer(KBId kbId) {
        return getKBCore(kbId.getId()).getIndexer(kbId.getModel());
    }

    public Searcher getSearcher(KBId kbId) {
        return getKBCore(kbId.getId()).getSearcher(kbId.getModel());
    }

    public void setKBInitSet(String ... kbIds) {
        this.kbInitSet.addAll(Arrays.asList(kbIds));
    }

    public void setDefaultIndicesFactory(IndicesFactory indicesFactory) {
        this.indicesFactory = Objects.requireNonNull(indicesFactory);
    }

    public void setDefaultGraphModelProviderFactory(GraphModelProviderFactory graphModelProviderFactory) {
        this.defaultGraphModelProviderFactory = Objects.requireNonNull(graphModelProviderFactory);
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
        logger.warn(marker, "No processors configured for {}", kbId);
        return null;
    }

    public synchronized final void initialize() {
        if (initialized) {
            throw new IllegalStateException("Core already initialized.");
        }

        this.initializeKBs();

        logger.info(marker, "Data root directory: '{}'", getDataRootDir());
        logger.info(marker, "Default Store Factory: '{}'", indicesFactory.getClass().getName());
        logger.info(marker, "DS Service Endpoint: '{}'", mainConfig.getString("distributional-service.rest-url"));
        logger.info(marker, "★☆ {}, {} ({}) ★☆", Version.getCodeName(), Version.getBuildVersion(), Version.getBuildNumber());
        initialized = true;
    }

    public synchronized final void terminate() {
        if (!initialized) {
            throw new IllegalStateException("Not initialized");
        }

        kbCoreMap.values().forEach(KBCore::terminate);
        initialized = false;
    }

    public DataProviderFactory getDataProviderFactory(KBId kbId) {
        try {
            String className = getDataProviderCfg(kbId).getString("class");
            Class<?> providerClazz = Class.forName(className);
            Constructor[] constructors = providerClazz.getConstructors();

            DataProviderFactory factory;
            if (BaseDataProviderFactory.class.isAssignableFrom(providerClazz)) {
                // It's our internal factory hence we inject the core dependency.
                factory = (DataProviderFactory) constructors[0].newInstance(this);
            } else {
                // This should be a user factory without constructor.
                // API user should rely on configuration or other means to initialize.
                // See TestDataProviderFactory as an example
                factory = (DataProviderFactory) providerClazz.newInstance();
            }

            return factory;
        } catch (Exception e) {
            throw new StarGraphException("Fail to initialize data provider factory: " + kbId, e);
        }
    }

    public GraphModelProviderFactory getGraphModelProviderFactory(String dbid) {
        String className;
        try {
            className = getGraphModelProviderCfg(dbid).getString("class");
        } catch (Exception e) {
            return defaultGraphModelProviderFactory;
        }

        try {
            Class<?> providerClazz = Class.forName(className);
            Constructor[] constructors = providerClazz.getConstructors();

            GraphModelProviderFactory factory;
            if (BaseGraphModelProviderFactory.class.isAssignableFrom(providerClazz)) {
                // It's our internal factory hence we inject the core dependency.
                factory = (GraphModelProviderFactory) constructors[0].newInstance(this);
            } else {
                // This should be a user factory without constructor.
                // API user should rely on configuration or other means to initialize.
                // See TestDataProviderFactory as an example
                factory = (GraphModelProviderFactory) providerClazz.newInstance();
            }

            return factory;
        } catch (Exception e) {
            throw new StarGraphException("Fail to initialize graph model provider factory: " + dbid, e);
        }
    }

    IndicesFactory getIndicesFactory(KBId kbId) {
        final String idxStorePath = "index-store.factory.class";
        if (kbId != null) {
            //from model configuration
            Config modelCfg = getModelConfig(kbId);
            if (modelCfg.hasPath(idxStorePath)) {
                String className = modelCfg.getString(idxStorePath);
                logger.info(marker, "Using '{}'.", className);
                return createIndicesFactory(className);
            }
        }

        if (indicesFactory == null) {
            //from main configuration if not already set
            indicesFactory = createIndicesFactory(getMainConfig().getString(idxStorePath));
        }

        return indicesFactory;
    }

    private boolean isEnabled(String dbId) {
        return getKBConfig(dbId).getBoolean("enabled");
    }

    private void initializeKBs() {
        if (!kbInitSet.isEmpty()) {
            logger.info(marker, "KB init set: {}", kbInitSet);
            kbInitSet.forEach(this::initializeKB);
        }
        else {
            if (mainConfig.hasPathOrNull("kb")) {
                if (mainConfig.getIsNull("kb")) {
                    throw new StarGraphException("No KB configured.");
                }

                mainConfig.getObject("kb").keySet().forEach(this::initializeKB);
            }
            else {
                throw new StarGraphException("No KBs configured.");
            }
        }
    }

    private void initializeKB(String kbName) {
        if (isEnabled(kbName)) {
            try {
                kbCoreMap.put(kbName, new KBCore(kbName, this, true));
            }
            catch (Exception e) {
                logger.error(marker, "Error starting '{}'", kbName, e);
            }
        }
        else {
            logger.warn(marker, "KB '{}' is disabled", kbName);
        }
    }

    private IndicesFactory createDefaultIndicesFactory() {
        return getIndicesFactory(null);
    }

    private IndicesFactory createIndicesFactory(String className) {
        try {
            Class<?> providerClazz = Class.forName(className);
            Constructor<?> constructor = providerClazz.getConstructors()[0];
            return (IndicesFactory) constructor.newInstance();
        } catch (Exception e) {
            throw new StarGraphException("Can't initialize indexers.", e);
        }
    }

    public EntitySearcher getEntitySearcher() {
        return entitySearcher;
    }


    public List<KBId> getKBIds(String dbId) {
        ConfigObject typeObj = getKBConfig(dbId).getObject("model");
        return typeObj.keySet().stream().map(modelName -> KBId.of(dbId, modelName)).collect(Collectors.toList());
    }

    // Configuration-Objects

    public Config getMainConfig() {
        return mainConfig;
    }

    public Config getKBConfig(String dbId) {
        return mainConfig.getConfig(String.format("kb.%s", dbId));
    }

    public Config getGraphModelConfig(String dbid) {
        return mainConfig.getConfig(String.format("kb.%s.graphmodel", dbid));
    }

    public Config getModelConfig(KBId kbId) {
        return mainConfig.getConfig(String.format("kb.%s.model.%s", kbId.getId(), kbId.getModel()));
    }

    public Config getGraphModelProviderCfg(String dbid) {
        return getGraphModelConfig(dbid).getConfig("provider");
    }

    public Config getDataProviderCfg(KBId kbId) {
        return getModelConfig(kbId).getConfig("provider");
    }

    public List<? extends Config> getProcessorsCfg(KBId kbId) {
        if (getModelConfig(kbId).hasPath("processors")) {
            return getModelConfig(kbId).getConfigList("processors");
        }
        return null;
    }

    // Directories

    public void setDataRootDir(String dataRootDir) {
        this.dataRootDir = Objects.requireNonNull(dataRootDir);
    }

    public void setDataRootDir(File dataRootDir) {
        this.dataRootDir = Objects.requireNonNull(dataRootDir.getAbsolutePath());
    }

    public String getDataRootDir() {
        return dataRootDir;
    }

    public String getKBDataDir(String dbId) {
        return Paths.get(getDataRootDir(), dbId).toString();
    }

    public String getGraphModelDataDir(String dbId) {
        return Paths.get(getDataRootDir(), dbId, "graph").toString();
    }

    public String getModelDataDir(KBId kbId) {
        return Paths.get(getDataRootDir(), kbId.getId(), kbId.getModel()).toString();
    }
}
