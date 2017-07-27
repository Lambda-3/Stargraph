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
import net.stargraph.model.Fact;
import net.stargraph.model.InstanceEntity;
import net.stargraph.model.PropertyEntity;

import java.io.Serializable;

/**
 * Can be placed in the workflow to filter out any triple that the value does not satisfy
 * the configured length conditions. Example that should be filtered: http://dbpedia.org/property/x.
 */
public final class LengthFilterProcessor extends BaseProcessor {
    public static String name = "length-filter";
    private int sMin, sMax = 0;
    private int pMin, pMax = 0;
    private int oMin, oMax = 0;

    public LengthFilterProcessor(Config config) {
        super(config);
        if (!getConfig().getIsNull("s")) {
            sMin = getConfig().getInt("s.min");
            sMax = getConfig().getInt("s.max");
        }
        if (!getConfig().getIsNull("p")) {
            pMin = getConfig().getInt("p.min");
            pMax = getConfig().getInt("p.max");
        }
        if (!getConfig().getIsNull("o")) {
            oMin = getConfig().getInt("o.min");
            oMax = getConfig().getInt("o.max");
        }
    }

    @Override
    public void doRun(Holder<Serializable> holder) throws ProcessorException {
        Serializable entry = holder.get();

        if (entry instanceof InstanceEntity) {
            String value = ((InstanceEntity) entry).getValue();
            holder.setSink(!inRange(value.length(), sMin, sMax));
        }
        else if (entry instanceof PropertyEntity) {
            String value = ((PropertyEntity) entry).getValue();
            holder.setSink(!inRange(value.length(), pMin, pMax));
        }
        else if (entry instanceof Fact) {
            Fact fact = (Fact) entry;
            if (fact.getSubject() instanceof InstanceEntity) {
                String value = ((InstanceEntity) fact.getSubject()).getValue();
                holder.setSink(!inRange(value.length(), sMin, sMax));
            }

            if (!holder.isSinkable()) {
                String value = fact.getPredicate().getValue();
                holder.setSink(!inRange(value.length(), pMin, pMax));
            }

            if (!holder.isSinkable()) {
                String value = fact.getObject().getValue();
                holder.setSink(!inRange(value.length(), oMin, oMax));
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    private static boolean inRange(int value, int min, int max) {
        return min <= 0 || max <= 0 || value >= min && value <= max;
    }
}
