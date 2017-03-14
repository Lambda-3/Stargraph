package net.stargraph.core.qa.annotator;

import net.stargraph.core.qa.Language;

import java.util.List;

public interface Annotator {

    List<Word> run(Language language, String sentence);

}
