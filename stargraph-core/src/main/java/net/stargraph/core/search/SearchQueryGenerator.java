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

import net.stargraph.model.ResourceEntity;
import net.stargraph.rank.ModifiableSearchParams;

import java.util.List;

public interface SearchQueryGenerator {

    // return facts that represent an is-a relationship between an arbitrary subject and an object's value matching the searchTerm
    SearchQueryHolder findClassFacts(ModifiableSearchParams searchParams);

    // return resource entities that match any of the given ids
    SearchQueryHolder entitiesWithIds(List<String> idList, ModifiableSearchParams searchParams);

    // return resource entities whose value matches the searchTerm (fuzzy)
    SearchQueryHolder findResourceInstances(ModifiableSearchParams searchParams, int maxEdits);

    // return properties with either the hyponyms, hypernyms or synonyms matching the searchTerm (why not including the value?)
    SearchQueryHolder findPropertyInstances(ModifiableSearchParams searchParams);

    // return facts that represent an arbitrary relationship with the pivot being either a subject or an object
    SearchQueryHolder findPivotFacts(ResourceEntity pivot, ModifiableSearchParams searchParams);

}
