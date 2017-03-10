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
import net.stargraph.data.processor.BaseProcessor;
import net.stargraph.data.processor.Holder;
import net.stargraph.data.processor.ProcessorException;
import net.stargraph.model.*;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class EntityClassifierProcessor extends BaseProcessor {
    public static String name = "entity-classifier";
    private static String relationStrType = "is-a";
    private List<String> relations;

    public EntityClassifierProcessor(Config config) {
        super(config);
        List<Object> list = getConfig().getList("relations").unwrapped();
        relations = list.stream().map(Objects::toString).collect(Collectors.toList());
    }

    @Override
    public void doRun(Holder<Serializable> holder) throws ProcessorException {
        Fact fact = (Fact) holder.get();
        PropertyEntity predicate = fact.getPredicate();
        LabeledEntity object = fact.getObject();

        if (relations.contains(predicate.getId())) {
            predicate = new PropertyEntity(relationStrType, relationStrType);
            if (object instanceof ValueEntity) {
                throw new IllegalStateException("Expected non-literal for a relation");
            } else {
                String[] terms = object.getValue().split("\\s+");
                object = new ClassEntity(object.getId(), object.getValue(), terms.length > 1);
            }

            holder.set(new Fact(fact.getKBId(), fact.getSubject(), predicate, object));
        }
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Attention: Assumes the usage is considered after this processor has being applied.
     */
    public static boolean isRelation(PropertyEntity p) {
        return p.getId().equalsIgnoreCase(relationStrType);
    }
}
