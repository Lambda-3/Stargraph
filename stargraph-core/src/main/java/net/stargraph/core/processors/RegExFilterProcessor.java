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
import net.stargraph.model.LabeledEntity;
import net.stargraph.model.PropertyEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RegExFilterProcessor extends BaseProcessor {
    public static String name = "regex-filter";
    private List<Pattern> subjExclusions;
    private List<Pattern> predExclusions;
    private List<Pattern> objExclusions;

    public RegExFilterProcessor(Config config) {
        super(config);

        if (!getConfig().getIsNull("s")) {
            subjExclusions = new ArrayList<>();
            List<String> strPatterns = getConfig().getStringList("s");
            strPatterns.forEach(p -> subjExclusions.add(Pattern.compile(p)));
        }

        if (!getConfig().getIsNull("p")) {
            predExclusions = new ArrayList<>();
            List<String> strPatterns = getConfig().getStringList("p");
            strPatterns.forEach(p -> predExclusions.add(Pattern.compile(p)));
        }

        if (!getConfig().getIsNull("o")) {
            objExclusions = new ArrayList<>();
            List<String> strPatterns = getConfig().getStringList("o");
            strPatterns.forEach(p -> objExclusions.add(Pattern.compile(p)));
        }
    }

    @Override
    public void doRun(Holder<Serializable> holder) throws ProcessorException {
        Fact fact = (Fact) holder.get();
        boolean excluded = false;
        if (subjExclusions != null) {
            InstanceEntity subj = (InstanceEntity) fact.getSubject();
            excluded = isExcluded(subjExclusions, subj.getId());
        }

        if (predExclusions != null && !excluded) {
            PropertyEntity pred = fact.getPredicate();
            excluded = isExcluded(predExclusions, pred.getId());
        }

        if (objExclusions != null && !excluded) {
            LabeledEntity obj = fact.getObject();
            excluded = isExcluded(objExclusions, obj.getId());
        }

        holder.setSink(excluded);
    }

    @Override
    public String getName() {
        return name;
    }

    private boolean isExcluded(List<Pattern> exclusions, String str) {
        return exclusions.parallelStream().anyMatch(pattern -> pattern.matcher(str).matches());
    }
}
