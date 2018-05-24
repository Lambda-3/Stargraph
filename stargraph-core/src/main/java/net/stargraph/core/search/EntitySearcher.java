package net.stargraph.core.search;

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

import net.stargraph.core.KBCore;
import net.stargraph.core.Namespace;
import net.stargraph.core.Stargraph;
import net.stargraph.model.BuiltInModel;
import net.stargraph.model.Fact;
import net.stargraph.model.ResourceEntity;
import net.stargraph.rank.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EntitySearcher {
    private static final int FUZZINESS = 1;

    private Logger logger = LoggerFactory.getLogger(getClass());
    private Marker marker = MarkerFactory.getMarker("elastic");

    private Stargraph stargraph;

    public EntitySearcher(Stargraph stargraph) {
        this.stargraph = stargraph;
    }

    public ResourceEntity getResourceEntity(String dbId, String id) {
        List<ResourceEntity> res = getResourceEntities(dbId, Collections.singletonList(id));
        if (res != null && !res.isEmpty()) {
            return res.get(0);
        }
        return null;
    }

    public List<ResourceEntity> getResourceEntities(String dbId, List<String> ids) {
        ModifiableSearchParams searchParams = ModifiableSearchParams.create(dbId).model(BuiltInModel.ENTITY);
        KBCore core = stargraph.getKBCore(dbId);

        logger.info(marker, "Fetching ids={}", ids);
        Namespace ns = core.getNamespace();
        List<String> idList = ids.stream().map(ns::shrinkURI).collect(Collectors.toList());

        SearchQueryGenerator searchQueryGenerator = core.getSearchQueryGenerator(searchParams.getKbId().getModel());
        SearchQueryHolder holder = searchQueryGenerator.entitiesWithIds(idList, searchParams);
        Searcher searcher = core.getSearcher(searchParams.getKbId().getModel());

        // Fetch initial candidates from the search engine
        Scores scores = searcher.search(holder);

        return scores.stream().map(s -> (ResourceEntity)s.getEntry()).collect(Collectors.toList());
    }

    public Scores classSearch(ModifiableSearchParams searchParams, ModifiableRankParams rankParams) {
        searchParams.model(BuiltInModel.FACT);
        KBCore core = stargraph.getKBCore(searchParams.getKbId().getId());

        if (rankParams instanceof ModifiableIndraParams) {
            core.configureDistributionalParams((ModifiableIndraParams) rankParams);
        }

        SearchQueryGenerator searchQueryGenerator = core.getSearchQueryGenerator(searchParams.getKbId().getModel());
        SearchQueryHolder holder = searchQueryGenerator.findClassFacts(searchParams);
        Searcher searcher = core.getSearcher(searchParams.getKbId().getModel());

        // Fetch initial candidates from the search engine
        Scores scores = searcher.search(holder);

        List<Score> classes2Score = scores.stream()
                .map(s -> new Score(((Fact)s.getEntry()).getObject(), s.getValue())).collect(Collectors.toList());
        // Re-Rank
        return Rankers.apply(new Scores(classes2Score), rankParams, searchParams.getSearchTerm());
    }

    public Scores resourceSearch(ModifiableSearchParams searchParams, ModifiableRankParams rankParams) {
        searchParams.model(BuiltInModel.ENTITY);
        KBCore core = stargraph.getKBCore(searchParams.getKbId().getId());

        SearchQueryGenerator searchQueryGenerator = core.getSearchQueryGenerator(searchParams.getKbId().getModel());
        SearchQueryHolder holder = searchQueryGenerator.findResourceInstances(searchParams, FUZZINESS);
        Searcher searcher = core.getSearcher(searchParams.getKbId().getModel());

        // Fetch initial candidates from the search engine
        Scores scores = searcher.search(holder);
        // Re-Rank
        return Rankers.apply(scores, rankParams, searchParams.getSearchTerm());
    }

    public Scores propertySearch(ModifiableSearchParams searchParams, ModifiableRankParams rankParams) {
        searchParams.model(BuiltInModel.PROPERTY);
        KBCore core = stargraph.getKBCore(searchParams.getKbId().getId());

        if (rankParams instanceof ModifiableIndraParams) {
            core.configureDistributionalParams((ModifiableIndraParams) rankParams);
        }

        SearchQueryGenerator searchQueryGenerator = core.getSearchQueryGenerator(searchParams.getKbId().getModel());
        SearchQueryHolder holder = searchQueryGenerator.findPropertyInstances(searchParams);
        Searcher searcher = core.getSearcher(searchParams.getKbId().getModel());

        // Fetch initial candidates from the search engine
        Scores scores = searcher.search(holder);
        // Re-Rank
        return Rankers.apply(scores, rankParams, searchParams.getSearchTerm());
    }

    public Scores pivotedSearch(ResourceEntity pivot,
                                ModifiableSearchParams searchParams, ModifiableRankParams rankParams) {
        searchParams.model(BuiltInModel.FACT);
        KBCore core = stargraph.getKBCore(searchParams.getKbId().getId());

        if (rankParams instanceof ModifiableIndraParams) {
            core.configureDistributionalParams((ModifiableIndraParams) rankParams);
        }

        SearchQueryGenerator searchQueryGenerator = core.getSearchQueryGenerator(searchParams.getKbId().getModel());
        SearchQueryHolder holder = searchQueryGenerator.findPivotFacts(pivot, searchParams);
        Searcher searcher = core.getSearcher(searchParams.getKbId().getModel());

        // Fetch initial candidates from the search engine
        Scores scores = searcher.search(holder);

        // We have to remap the facts to properties, the real target of the ranker call.
        // Thus we're discarding the score values from the underlying search engine. Shall we?
        Scores propScores = new Scores(scores.stream()
                .map(s -> ((Fact) s.getEntry()).getPredicate())
                .distinct()
                .map(p -> new Score(p, 0))
                .collect(Collectors.toList()));

        return Rankers.apply(propScores, rankParams, searchParams.getSearchTerm());
    }
}
