package net.stargraph.core.impl.corenlp;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import net.stargraph.UnsupportedLanguageException;
import net.stargraph.core.query.annotator.Annotator;
import net.stargraph.core.query.annotator.PartOfSpeechSet;
import net.stargraph.core.query.annotator.Word;
import net.stargraph.query.Language;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.stargraph.query.Language.EN;

public final class CoreNLPAnnotator extends Annotator {

    private Map<Language, MaxentTagger> taggers;

    public CoreNLPAnnotator() {
        taggers = new ConcurrentHashMap<>();
    }


    @Override
    protected List<Word> doRun(Language language, String sentence) {
        MaxentTagger tagger = taggers.computeIfAbsent(language, lang -> {
            if (lang == EN) {
                return new MaxentTagger("edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
            }
            throw new UnsupportedLanguageException(lang);
        });

        PartOfSpeechSet partOfSpeechSet = PartOfSpeechSet.getPOSSet(language);
        List<Word> words = new ArrayList<>();

        List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(sentence));
        sentences.forEach(s -> {
            tagger.tagSentence(s).forEach(taggedWord ->
                    words.add(new Word(partOfSpeechSet.valueOf(taggedWord.tag()), taggedWord.value())));
        });

        return words;
    }

}
