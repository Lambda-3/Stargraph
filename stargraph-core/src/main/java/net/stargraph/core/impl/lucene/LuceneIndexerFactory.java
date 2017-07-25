package net.stargraph.core.impl.lucene;

import net.stargraph.core.Stargraph;
import net.stargraph.core.index.BaseIndexer;
import net.stargraph.core.index.IndexerFactory;
import net.stargraph.model.KBId;

public final class LuceneIndexerFactory implements IndexerFactory {

    @Override
    public BaseIndexer create(KBId kbId, Stargraph core) {
        return new LuceneIndexer(kbId, core);
    }
}
