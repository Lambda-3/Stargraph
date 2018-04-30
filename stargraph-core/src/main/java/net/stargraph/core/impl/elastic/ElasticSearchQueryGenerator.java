package net.stargraph.core.impl.elastic;

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

import net.stargraph.core.search.SearchQueryGenerator;
import net.stargraph.core.search.SearchQueryHolder;
import net.stargraph.model.InstanceEntity;
import net.stargraph.rank.ModifiableSearchParams;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.*;

public class ElasticSearchQueryGenerator implements SearchQueryGenerator {

    @Override
    public SearchQueryHolder findClassFacts(ModifiableSearchParams searchParams) {

        QueryBuilder queryBuilder = boolQuery()
                .must(nestedQuery("p",
                        termQuery("p.id", "is-a"),  ScoreMode.Max))
                .should(nestedQuery("o",
                        matchQuery("o.value", searchParams.getSearchTerm()),  ScoreMode.Max))
                .minimumShouldMatch("1");

        return new ElasticQueryHolder(queryBuilder, searchParams);
    }

    @Override
    public SearchQueryHolder entitiesWithIds(List idList, ModifiableSearchParams searchParams) {
        QueryBuilder queryBuilder = termsQuery("id", idList);

        return new ElasticQueryHolder(queryBuilder, searchParams);
    }

    @Override
    public SearchQueryHolder findEntityInstances(ModifiableSearchParams searchParams) {
        QueryBuilder queryBuilder = matchQuery("value", searchParams.getSearchTerm())
                .fuzziness(0).fuzzyTranspositions(false).operator(Operator.AND);

        return new ElasticQueryHolder(queryBuilder, searchParams);
    }

    @Override
    public SearchQueryHolder findPropertyInstances(ModifiableSearchParams searchParams) {
        QueryBuilder queryBuilder = boolQuery()
                .should(nestedQuery("hyponyms",
                        matchQuery("hyponyms.word", searchParams.getSearchTerm()), ScoreMode.Max))
                .should(nestedQuery("hypernyms",
                        matchQuery("hypernyms.word", searchParams.getSearchTerm()), ScoreMode.Max))
                .should(nestedQuery("synonyms",
                        matchQuery("synonyms.word", searchParams.getSearchTerm()), ScoreMode.Max))
                .minimumNumberShouldMatch(1);

        return new ElasticQueryHolder(queryBuilder, searchParams);
    }

    @Override
    public SearchQueryHolder findPivotFacts(InstanceEntity pivot, ModifiableSearchParams searchParams) {
        QueryBuilder queryBuilder = boolQuery()
                .should(nestedQuery("s", termQuery("s.id", pivot.getId()), ScoreMode.Max))
                .should(nestedQuery("o", termQuery("o.id", pivot.getId()), ScoreMode.Max)).minimumNumberShouldMatch(1);

        return new ElasticQueryHolder(queryBuilder, searchParams);
    }
}
