package net.stargraph.core.impl.opennlp;

import com.typesafe.config.Config;
import net.stargraph.Language;
import net.stargraph.StarGraphException;
import net.stargraph.core.query.annotator.Annotator;
import net.stargraph.core.query.annotator.PartOfSpeechSet;
import net.stargraph.core.query.annotator.Word;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class OpenNLPAnnotator extends Annotator {
    private ConcurrentHashMap<Language, TokenizerModel> tokenizerModels;
    private ConcurrentHashMap<Language, POSModel> posModels;
    private File modelsDir;

    public OpenNLPAnnotator(Config config) {
        this.tokenizerModels = new ConcurrentHashMap<>();
        this.posModels = new ConcurrentHashMap<>();
        this.modelsDir = new File(Objects.requireNonNull(config).getString("opennlp.models-dir"));
        logger.debug(marker, "Models dir: {}", modelsDir);
    }

    @Override
    public List<Word> doRun(Language language, String sentence) {
        Tokenizer tokenizer = new TokenizerME(getTokenizerModel(language));
        POSTaggerME tagger = new POSTaggerME(getPOSModel(language));
        String[] tokens = tokenizer.tokenize(sentence);
        String[] tags = tagger.tag(tokens);

        PartOfSpeechSet posSet = PartOfSpeechSet.getPOSSet(language);

        List<Word> words = new ArrayList<>();
        for (int i = 0; i < tokens.length; i++) {
            words.add(new Word(posSet.valueOf(tags[i]), tokens[i]));
        }

        return words;
    }

    private TokenizerModel getTokenizerModel(Language language) {
        return tokenizerModels.computeIfAbsent(language, this::readTokenizerModel);
    }

    private POSModel getPOSModel(Language language) {
        return posModels.computeIfAbsent(language, this::readPOSModel);
    }

    private TokenizerModel readTokenizerModel(Language language) {
        logger.debug(marker, "Reading tokenizer model for {}", language);
        File modelFile = new File(modelsDir, String.format("%s-token.bin", language.name().toLowerCase()));
        try (InputStream in = new FileInputStream(modelFile)) {
            return new TokenizerModel(in);
        } catch (IOException e) {
            throw new StarGraphException("Can't read '" + modelFile + "'", e);
        }
    }

    private POSModel readPOSModel(Language language) {
        logger.debug(marker, "Reading POS model for {}", language);
        File modelFile = new File(modelsDir, String.format("%s-pos-maxent.bin", language.name().toLowerCase()));
        try (InputStream in = new FileInputStream(modelFile)) {
            return new POSModel(in);
        } catch (IOException e) {
            throw new StarGraphException("Can't read '" + modelFile + "'", e);
        }
    }
}
