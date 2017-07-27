package net.stargraph.core;

import net.stargraph.core.index.BaseIndexer;
import net.stargraph.core.search.BaseSearcher;
import net.stargraph.model.KBId;

public interface IndicesFactory {

    BaseIndexer createIndexer(KBId kbId, Stargraph core);

    BaseSearcher createSearcher(KBId kbId, Stargraph core);
}
