package net.stargraph.core.impl.lucene;

import net.stargraph.StarGraphException;
import net.stargraph.core.search.SearchQueryGenerator;
import net.stargraph.core.search.SearchQueryHolder;
import net.stargraph.model.ResourceEntity;
import net.stargraph.rank.ModifiableSearchParams;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.TermsQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.search.Query;

import java.util.ArrayList;
import java.util.List;

public class LuceneSearchQueryGenerator implements SearchQueryGenerator {

    @Override
    public SearchQueryHolder findClassFacts(ModifiableSearchParams searchParams) {
        //TODO implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public SearchQueryHolder entitiesWithIds(List<String> idList, ModifiableSearchParams searchParams) {
        List<Term> terms = new ArrayList<>();
        for (String id : idList) {
            terms.add(new Term("id", id));
        }
        Query query = new TermsQuery(terms);

        return new LuceneQueryHolder(query, searchParams);
    }

    @Override
    public SearchQueryHolder findResourceInstances(ModifiableSearchParams searchParams, int maxEdits) {
        //Query query = new FuzzyQuery(new Term("value", searchParams.getSearchTerm()), maxEdits, 0, 50, false); // This does not take into account multiple words of the search term
        Query query = fuzzyPhraseSearch("value", searchParams.getSearchTerm(), maxEdits);

        return new LuceneQueryHolder(query, searchParams);
    }

    @Override
    public SearchQueryHolder findPropertyInstances(ModifiableSearchParams searchParams) {
        //TODO implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public SearchQueryHolder findPivotFacts(ResourceEntity pivot, ModifiableSearchParams searchParams) {
        //TODO implement
        throw new UnsupportedOperationException("Not implemented yet");
    }


    private static Query fuzzyPhraseSearch(String field, String searchTerm, int maxEdits) {
        StringBuilder queryStr = new StringBuilder();
        queryStr.append(field).append(":(");

        String words[] = searchTerm.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                queryStr.append(" AND ");
            }
            queryStr.append(ComplexPhraseQueryParser.escape(words[i])).append("~").append(maxEdits);
        }
        queryStr.append(")");

        try {
            return new ComplexPhraseQueryParser("value", new StandardAnalyzer()).parse(queryStr.toString());
        } catch (ParseException e) {
            throw new StarGraphException(e);
        }
    }
}
