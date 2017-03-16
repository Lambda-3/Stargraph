package net.stargraph.core.qa.annotator;

import net.stargraph.Language;

import java.util.List;

public interface Annotator {

    List<Word> run(Language language, String sentence);

}
