package net.stargraph.core.impl.lucene;

import net.stargraph.core.IndicesFactory;
import net.stargraph.core.Stargraph;
import net.stargraph.core.index.BaseIndexer;
import net.stargraph.core.search.BaseSearcher;
import net.stargraph.model.KBId;

public final class LuceneIndicesFactory implements IndicesFactory {

    @Override
    public BaseIndexer createIndexer(KBId kbId, Stargraph core) {
        return new LuceneIndexer(kbId, core);
    }

    @Override
    public BaseSearcher createSearcher(KBId kbId, Stargraph core) {
        return new LuceneSearcher(kbId, core);
    }
}
