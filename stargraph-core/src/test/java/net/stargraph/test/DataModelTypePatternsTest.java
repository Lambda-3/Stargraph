package net.stargraph.test;

import net.stargraph.core.qa.annotator.Word;
import net.stargraph.core.qa.nli.AnalysisStep;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static net.stargraph.core.qa.annotator.EnglishPOSSet.*;

public class DataModelTypePatternsTest {

    @Test
    public void updateTest() {
        List<Word> annotated = Arrays.asList(
                new Word(WP, "Who"),
                new Word(VBZ, "is"),
                new Word(DT, "the"),
                new Word(NN, "wife"),
                new Word(IN, "of"),
                new Word(NNP, "Barack"),
                new Word(NNP, "Obama"),
                new Word(PUNCT, "?"));

        AnalysisStep view = new AnalysisStep(annotated);
        System.out.println(view);
    }

}
