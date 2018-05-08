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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.typesafe.config.Config;
import net.stargraph.StarGraphException;
import net.stargraph.model.ClassEntity;
import net.stargraph.model.InstanceEntity;
import net.stargraph.model.PropertyEntity;
import net.stargraph.model.ValueEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public final class Namespace extends TreeMap<String, String> {
    private static Logger logger = LoggerFactory.getLogger(Namespace.class);
    private static Marker marker = MarkerFactory.getMarker("core");

    private Config kbConfig;
    private Set<String> mainNamespaces;
    private Cache<String, String> shortenedURICache;

    private Namespace(Config kbConfig) {
        super((s1, s2) -> s1.length() == s2.length() ? s1.compareTo(s2) : s2.length() - s1.length()); // reverse order
        this.kbConfig = Objects.requireNonNull(kbConfig);
        this.readMainNamespaces();
        this.readMappings();
        initCache();
    }

    private Namespace(String resource) {
        super((s1, s2) -> s1.length() == s2.length() ? s1.compareTo(s2) : s2.length() - s1.length()); // reverse order
        putAll(readNamespaceResource(resource));
        initCache();
    }

    private void initCache() {
        // Be aware: Facts for a given Entity may appear 1k times (like a db entry with 1k columns!).
        // Not following this assumption may result in a poorer performance, specially while bulk loading.
        this.shortenedURICache = CacheBuilder.newBuilder().maximumSize(1000).build();
    }

    public String shrinkURI(String uri) {
        try {
            return shortenedURICache.get(uri, () -> {
                // This is the computation we want to avoid using the cache.
                if (uri.startsWith("http://")) {
                    for (Map.Entry<String, String> entry : this.entrySet()) {
                        if (uri.startsWith(entry.getKey())) {
                            return uri.replace(entry.getKey(), entry.getValue());
                        }
                    }
                }

                return uri;
            });
        } catch (ExecutionException e) {
            throw new StarGraphException(e);
        }
    }

    public String expandURI(String uri) {
        for (Map.Entry<String, String> entry : this.entrySet()) {
            if (uri.startsWith(entry.getValue())) {
                return uri.replace(entry.getValue(), entry.getKey());
            }
        }
        return uri;
    }

    public static Namespace createDefault() {
        return new Namespace("default");
    }

    public boolean isFromMainNS(String uri) {
        String[] mapped = shrinkURI(uri).split(":");
        return mainNamespaces.contains(mapped[0]);
    }

    @SuppressWarnings("unchecked")
    public  <S extends Serializable> S expand(S entry) {
        if (entry == null) {
            throw new IllegalArgumentException("Entry can't be null");
        }

        if (entry instanceof ValueEntity) {
            return entry;
        } else if (entry instanceof InstanceEntity) {
            InstanceEntity e = (InstanceEntity) entry;
            return (S) new InstanceEntity(expandURI(e.getId()), e.getValue(), e.getOtherValues());
        } else if (entry instanceof ClassEntity) {
            ClassEntity e = (ClassEntity) entry;
            return (S) new ClassEntity(expandURI(e.getId()), e.getValue(), e.isComplex());
        } else if (entry instanceof PropertyEntity) {
            PropertyEntity e = (PropertyEntity) entry;
            return (S) new PropertyEntity(expandURI(e.getId()), e.getValue(), e.getHypernyms(), e.getHyponyms(), e.getSynonyms());
        } else {
            throw new IllegalArgumentException("Unknown type instance to expand namespace: " + entry.getClass());
        }
    }

    static Namespace create(Config kbConfig) {
        return new Namespace(kbConfig);
    }

    private static BufferedReader getReader(String resource) {
        try {
            InputStream is = ClassLoader.getSystemResourceAsStream(resource);
            return new BufferedReader(new InputStreamReader(is));
        } catch (Exception e) {
            throw new StarGraphException("Fail reading from '" + resource + "'", e);
        }
    }

    private void readMainNamespaces() {
        mainNamespaces = new LinkedHashSet<>();
        if (kbConfig.hasPath("namespaces")) {
            Config nsConfig = kbConfig.getConfig("namespaces");
            mainNamespaces.addAll(nsConfig.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList()));
            logger.info(marker, "Main Namespaces: {}", mainNamespaces);
        }
    }

    private void readMappings() {
        final String key = "graphmodel.namespace.mapping";
        if (kbConfig.hasPath(key) && !kbConfig.getIsNull(key)) {
            String resource = kbConfig.getString(key);
            putAll(readNamespaceResource(resource));
        } else {
            logger.warn(marker, "No default namespace mappings configured.");
        }
    }

    private static Map<String, String> readNamespaceResource(String resource) {
        Map<String, String> all = new HashMap<>();
        String classPathResource = String.format("%s-namespace.txt", Objects.requireNonNull(resource));
        logger.info(marker, "Namespace resource: {}", classPathResource);

        try (BufferedReader buf = getReader(classPathResource)) {
            buf.lines().forEach(line -> {
                try {
                    String[] s = line.split(" ");
                    all.put(s[1], s[0] + ":");
                } catch (Exception e) {
                    logger.error(marker, "Error reading {}", line, e);
                }
            });

        } catch (Exception e) {
            throw new StarGraphException("Can't initialize Namespace map", e);
        }

        return all;
    }
}
