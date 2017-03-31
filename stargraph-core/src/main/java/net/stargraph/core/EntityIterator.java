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

import com.google.common.collect.Iterators;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import net.stargraph.data.Indexable;
import net.stargraph.model.KBId;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.*;

import static net.stargraph.ModelUtils.createInstance;

public final class EntityIterator implements Iterator<Indexable> {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Marker marker = MarkerFactory.getMarker("core");

    private KBId kbId;
    private Stargraph core;
    private Set<String> mainNamespaces;
    private Namespace namespace;
    private Iterator<Node> iterator;
    private Node currentNode;

    public EntityIterator(Stargraph core, KBId kbId) {
        this.kbId = Objects.requireNonNull(kbId);
        this.core = core;
        this.mainNamespaces = buildMainNamespaces();
        this.namespace = Namespace.create(core, kbId.getId());
        this.iterator = createIterator();
    }


    @Override
    public boolean hasNext() {
        if (currentNode != null) {
            return true;
        }

        while (iterator.hasNext()) {
            currentNode = iterator.next();
            //skipping literals and blank nodes.
            if ((!currentNode.isBlank() && !currentNode.isLiteral() && isFromMainNS())) {
                return true;
            }
        }

        currentNode = null;
        return false;
    }

    @Override
    public Indexable next() {
        try {
            if (currentNode == null) {
                throw new NoSuchElementException();
            }
            return new Indexable(createInstance(applyNS(currentNode.getURI())), kbId);
        } finally {
            currentNode = null;
        }
    }

    private String applyNS(String uri) {
        if (namespace != null) {
            return namespace.map(uri);
        }
        return uri;
    }

    private boolean isFromMainNS() {
        return mainNamespaces.parallelStream().anyMatch(ns -> currentNode.getURI().startsWith(ns));
    }

    private Set<String> buildMainNamespaces() {
        Config kbConfig = core.getKBConfig(kbId.getId());
        if (kbConfig.hasPath("namespaces")) {
            Config nsConfig = core.getKBConfig(kbId.getId()).getConfig("namespaces");
            Set<String> nsSet = new LinkedHashSet<>();
            for (Map.Entry<String, ConfigValue> e : nsConfig.entrySet()) {
                nsSet.add(e.getKey());
                nsSet.add((String) e.getValue().unwrapped());
            }
            logger.info(marker, "Main Namespaces: {}", nsSet);
            return nsSet;
        }

        return null;
    }

    private Iterator<Node> createIterator() {
        Model model = core.getGraphModel(kbId.getId());
        Graph g = model.getGraph();
        ExtendedIterator<Triple> exIt = g.find(Node.ANY, null, null);
        ExtendedIterator<Node> subjIt = exIt.mapWith(Triple::getSubject);
        ExtendedIterator<Node> predIt = exIt.mapWith(Triple::getObject);
        return Iterators.concat(subjIt, predIt);
    }
}
