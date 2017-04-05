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

    private String annotate(String sentence) {
        List<Word> words = annotator.run(Language.EN, sentence);
        return words.stream().map(Word::toString).collect(Collectors.joining(" "));
    }
}
