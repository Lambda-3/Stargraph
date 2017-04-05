package net.stargraph.test;

/*-
 * ==========================License-Start=============================
 * stargraph-core
 * --------------------------------------------------------------------
 * Copyright (C) 2017 Lambda^3
 * --------------------------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ==========================License-End===============================
 */

import com.typesafe.config.ConfigFactory;
import net.stargraph.core.impl.opennlp.OpenNLPAnnotator;
import net.stargraph.core.query.annotator.Annotator;
import net.stargraph.core.query.annotator.Word;
import net.stargraph.query.Language;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

public final class EnglishPOSTest {

    private Annotator annotator;

    @BeforeClass
    public void beforeClass() {
        annotator = new OpenNLPAnnotator(ConfigFactory.load().getConfig("stargraph"));
    }

    @Test
    public void s0() {
        Assert.assertEquals(annotate("Who is the wife of Barack Obama?"),
                "Who/WP is/VBZ the/DT wife/NN of/IN Barack/NNP Obama/NNP ?/.");
    }

    @Test
    public void s1() {
        Assert.assertEquals(annotate("Which river does the Brooklyn Bridge cross?"),
                "Which/WDT river/NN does/VBZ the/DT Brooklyn/NNP Bridge/NNP cross/VB ?/.");
    }

    @Test
    public void s2() {
        Assert.assertEquals(annotate("Who created the comic Captain America?"),
                "Who/WP created/VBD the/DT comic/JJ Captain/NNP America/NNP ?/.");
    }

    @Test
    public void s3() {
        Assert.assertEquals(annotate("Who is the president of Brazil?"),
                "Who/WP is/VBZ the/DT president/NN of/IN Brazil/NNP ?/.");
    }

    @Test
    public void s4() {
        Assert.assertEquals(annotate("Who is the mayor of Berlin?"),
                "Who/WP is/VBZ the/DT mayor/NN of/IN Berlin/NNP ?/.");
    }

    @Test
    public void s5() {
        Assert.assertEquals(annotate("Give me the albedo of the Moon."),
                "Give/VB me/PRP the/DT albedo/NN of/IN the/DT Moon/NNP ./.");
    }

    @Test
    public void s6() {
        Assert.assertEquals(annotate("Give me all movies directed by Francis Ford Coppola."),
                "Give/VB me/PRP all/DT movies/NNS directed/VBN by/IN Francis/NNP Ford/NNP Coppola/NNP ./.");
    }

    @Test
    public void s7() {
        Assert.assertEquals(annotate("How many inhabitants does Maribor have?"),
                "How/WRB many/JJ inhabitants/NNS does/VBZ Maribor/NNP have/VB ?/.");
    }

    @Test
    public void s8() {
        Assert.assertEquals(annotate("Which country does the creator of Miffy come from?"),
                "Which/WDT country/NN does/VBZ the/DT creator/NN of/IN Miffy/NNP come/VB from/IN ?/.");
    }

    @Test
    public void s9() {
        Assert.assertEquals(annotate("What is the area code of Berlin?"),
                "What/WP is/VBZ the/DT area/NN code/NN of/IN Berlin/NNP ?/.");
    }

    @Test
    public void s10() {
        Assert.assertEquals(annotate("In which programming language is GIMP written?"),
                "In/IN which/WDT programming/NNP language/NN is/VBZ GIMP/NNP written/VBN ?/.");
    }

    @Test
    public void s11() {
        Assert.assertEquals(annotate("What is the mean temperature in Jupiter?"),
                "What/WP is/VBZ the/DT mean/JJ temperature/NN in/IN Jupiter/NNP ?/.");
    }

    @Test
    public void s12() {
        Assert.assertEquals(annotate("Which ingredients do I need for carrot cake?"),
                "Which/WDT ingredients/NN do/VBP I/PRP need/VB for/IN carrot/NN cake/NN ?/.");
    }

    @Test
    public void s13() {
        Assert.assertEquals(annotate("Who succeeded Pope John Paul II?"),
                "Who/WP succeeded/VBD Pope/NNP John/NNP Paul/NNP II/NNP ?/.");
    }

    @Test
    public void s14() {
        Assert.assertEquals(annotate("Which bridges cross the River Seine?"),
                "Which/WDT bridges/NN cross/VBP the/DT River/NNP Seine/NNP ?/.");
    }

    @Test
    public void s15() {
        Assert.assertEquals(annotate("What is the cost of Boeing 777?"),
                "What/WP is/VBZ the/DT cost/NN of/IN Boeing/NNP 777/NNP ?/.");
    }

    @Test
    public void s16() {
        Assert.assertEquals(annotate("Which awards did Douglas Hofstadter win?"),
                "Which/WDT awards/NN did/VBD Douglas/NNP Hofstadter/NNP win/VB ?/.");
    }

    private String annotate(String sentence) {
        List<Word> words = annotator.run(Language.EN, sentence);
        return words.stream().map(Word::toString).collect(Collectors.joining(" "));
    }
}
