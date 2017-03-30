package net.stargraph.test;

import com.typesafe.config.ConfigFactory;
import net.stargraph.UnsupportedLanguageException;
import net.stargraph.core.impl.opennlp.OpenNLPAnnotator;
import net.stargraph.query.Language;
import net.stargraph.core.query.annotator.Annotator;
import net.stargraph.core.query.annotator.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static net.stargraph.core.query.annotator.EnglishPOSSet.*;

public final class AnnotatorTest {

    @Test
    public void englishTest() {
        Annotator annotator = new OpenNLPAnnotator(ConfigFactory.load().getConfig("stargraph"));
        List<Word> annotatedWords = annotator.run(Language.EN, "Of course this can work seamless!");
        List<Word> expected = Arrays.asList(
                new Word(IN, "Of"),
                new Word(NN, "course"),
                new Word(DT, "this"),
                new Word(MD, "can"),
                new Word(VB, "work"),
                new Word(RB, "seamless"),
                new Word(PUNCT, "!"));

        Assert.assertEquals(annotatedWords, expected);
    }

    @Test(expectedExceptions = UnsupportedLanguageException.class)
    public void germanTest() {
        Annotator annotator = new OpenNLPAnnotator(ConfigFactory.load().getConfig("stargraph"));
        annotator.run(Language.DE, "Wir haben zusammen noch keine Schweine gehütet!");
    }
}
