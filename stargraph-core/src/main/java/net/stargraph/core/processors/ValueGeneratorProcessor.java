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
import com.typesafe.config.ConfigObject;
import net.stargraph.StarGraphException;
import net.stargraph.core.Stargraph;
import net.stargraph.core.ner.LinkedNamedEntity;
import net.stargraph.core.ner.NER;
import net.stargraph.core.search.EntitySearcher;
import net.stargraph.data.processor.BaseProcessor;
import net.stargraph.data.processor.Holder;
import net.stargraph.data.processor.ProcessorException;
import net.stargraph.model.*;
import net.stargraph.query.Language;
import net.stargraph.rank.*;
import org.lambda3.text.simplification.discourse.utils.sentences.SentencesUtils;

import javax.sql.rowset.Predicate;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Can be placed in the workflow to create passages.
 */
public final class ValueGeneratorProcessor extends BaseProcessor {
    public static String name = "value-generator";

    private Stargraph stargraph;
    private EntitySearcher entitySearcher;
    private Map<Language, String> searchTerms;
    private double threshold;

    public ValueGeneratorProcessor(Stargraph stargraph, Config config) {
        super(config);
        this.stargraph = stargraph;
        this.entitySearcher = stargraph.getEntitySearcher();

        if (!getConfig().getIsNull("search-terms")) {
            this.searchTerms = loadSearchTerms(getConfig());
        }
        if (!getConfig().getIsNull("threshold")) {
            this.threshold = getConfig().getDouble("threshold");
        }
    }

    @Override
    public void doRun(Holder<Serializable> holder) throws ProcessorException {
        Serializable entry = holder.get();

        if (entry instanceof ResourceEntity) {
            ResourceEntity entity = (ResourceEntity)entry;
            Language language = stargraph.getKBCore(holder.getKBId().getId()).getLanguage();
            if (!searchTerms.containsKey(language)) {
                throw new StarGraphException("No search-term specified for language: " + language);
            }
            String searchTerm = searchTerms.get(language);

            ModifiableSearchParams searchParams = ModifiableSearchParams.create(holder.getKBId().getId()).term(searchTerm);
            ModifiableRankParams rankParams = ParamsBuilder.word2vec().threshold(Threshold.min(threshold));
            Scores scores = entitySearcher.pivotedSearch(entity, searchParams, rankParams, true);

            List<String> otherValues = scores.stream()
                    .filter(s -> s.getEntry() instanceof ValueEntity)
                    .map(s -> ((ValueEntity)s.getEntry()).getValue())
                    .collect(Collectors.toList());

            holder.set(new ResourceEntity(entity.getId(), entity.getValue(), otherValues));
        }
    }

    @Override
    public String getName() {
        return name;
    }

    private Map<Language, String> loadSearchTerms(Config config) {
        Map<Language, String> termsByLang = new LinkedHashMap<>();
        ConfigObject configObject = config.getObject("search-terms");

        configObject.keySet().forEach(strLang -> {
            Language language = Language.valueOf(strLang.toUpperCase());
            String term = configObject.toConfig().getString(strLang);

            termsByLang.put(language, term);
        });

        return termsByLang;
    }
}
