package net.stargraph.core.query;

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

import net.stargraph.StarGraphException;
import net.stargraph.query.InteractionMode;


public final class EntityQueryBuilder {

    private final String IS_LIKE = "like";
    private final String IS_SIMILAR_TO = "similar to";

    public EntityQueryBuilder(){

    }

    public EntityQuery parse(String queryString, InteractionMode mode){

        EntityQuery entityQuery;

        switch (mode) {
            case ENTITY_SIMILARITY:
                entityQuery = parseSimilarityQuery(queryString);
                break;
            case DEFINITION:
                entityQuery = parseDefinitionQuery(queryString);
                break;
            default:
                throw new StarGraphException("Input type not yet supported");
        }

        return entityQuery;
    }

    private EntityQuery parseSimilarityQuery(String queryString){

        String coreEntity = "";

        if(queryString.contains(IS_SIMILAR_TO))
            coreEntity = queryString.substring(queryString.indexOf(IS_SIMILAR_TO) + IS_SIMILAR_TO.length(), queryString.length()).trim();
        if(queryString.contains(IS_LIKE))
            coreEntity = queryString.substring(queryString.indexOf(IS_LIKE) + IS_LIKE.length(), queryString.length()).trim();

        return new EntityQuery(coreEntity);

    }

    private EntityQuery parseDefinitionQuery(String queryString){

        String coreEntity = "";

        coreEntity = queryString.replace("Who is", "").replace("Who are", "").
                replace("What is", "").replace("What are", "").replace("Define", "").replace("\\?", "").trim();

        return new EntityQuery(coreEntity);
    }

}
