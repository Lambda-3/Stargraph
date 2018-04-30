package net.stargraph.core.impl.lucene;

import net.stargraph.core.search.SearchQueryGenerator;
import net.stargraph.core.search.SearchQueryHolder;
import net.stargraph.model.InstanceEntity;
import net.stargraph.rank.ModifiableSearchParams;

import java.util.List;

public class LuceneSearchQueryGenerator implements SearchQueryGenerator {

    @Override
    public SearchQueryHolder findClassFacts(ModifiableSearchParams searchParams) {
        //TODO implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public SearchQueryHolder entitiesWithIds(List idList, ModifiableSearchParams searchParams) {
        //TODO implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public SearchQueryHolder findEntityInstances(ModifiableSearchParams searchParams) {
        //TODO implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public SearchQueryHolder findPropertyInstances(ModifiableSearchParams searchParams) {
        //TODO implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public SearchQueryHolder findPivotFacts(InstanceEntity pivot, ModifiableSearchParams searchParams) {
        //TODO implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
