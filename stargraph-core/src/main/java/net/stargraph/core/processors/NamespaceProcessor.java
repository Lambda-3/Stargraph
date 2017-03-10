package net.stargraph.core.processors;

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
import com.typesafe.config.ConfigValue;
import net.stargraph.StarGraphException;
import net.stargraph.data.processor.BaseProcessor;
import net.stargraph.data.processor.Holder;
import net.stargraph.data.processor.ProcessorException;
import net.stargraph.model.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public final class NamespaceProcessor extends BaseProcessor {
    public static String name = "namespace";
    private Map<String, String> prefixes;

    public NamespaceProcessor(Config config) {
        super(config);
        prefixes = new HashMap<>();
        for (Map.Entry<String, ConfigValue> e : getConfig().entrySet()) {
            prefixes.put((String) e.getValue().unwrapped(), e.getKey() + ":");
        }
    }

    @Override
    public void doRun(Holder<Serializable> holder) throws ProcessorException {
        holder.set(resolve(holder.get()));
    }

    @Override
    public String getName() {
        return name;
    }

    private String map(String uri) {
        for (Map.Entry<String, String> entry : prefixes.entrySet()) {
            if (uri.startsWith(entry.getKey())) {
                return uri.replace(entry.getKey(), entry.getValue());
            }
        }
        return uri;
    }

    private Serializable resolve(Serializable o) {
        if (o instanceof Fact) {
            Fact fact = (Fact) o;
            ContextId subject = fact.getSubject();
            PropertyEntity predicate = fact.getPredicate();
            LabeledEntity object = fact.getObject();

            if (subject instanceof InstanceEntity) {
                InstanceEntity sub = (InstanceEntity) subject;
                subject = new InstanceEntity(map(sub.getId()), sub.getValue(), sub.getOtherValues());
            }

            predicate = new PropertyEntity(map(predicate.getId()),
                    predicate.getValue(), predicate.getHypernyms(), predicate.getHyponyms(), predicate.getSynonyms());

            if (object instanceof InstanceEntity) {
                InstanceEntity obj = (InstanceEntity) object;
                object = new InstanceEntity(map(obj.getId()), obj.getValue(), obj.getOtherValues());
            } else if (object instanceof ClassEntity) {
                ClassEntity obj = (ClassEntity) object;
                object = new ClassEntity(map(obj.getId()), obj.getValue(), obj.isComplex());
            }

            return new Fact(fact.getKBId(), subject, predicate, object);
        }

        if (o instanceof PropertyEntity) {
            PropertyEntity predicate = (PropertyEntity) o;
            return new PropertyEntity(map(predicate.getId()),
                    predicate.getValue(), predicate.getHypernyms(), predicate.getHyponyms(), predicate.getSynonyms());
        }

        throw new StarGraphException("Unsupported type '" + o.getClass() + "'");
    }
}
