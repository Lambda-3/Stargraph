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
