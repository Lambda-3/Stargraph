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

import net.stargraph.core.query.QueryEngine;
import net.stargraph.core.query.nli.ClueAnalyzer;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(enabled = false) // A lot to do in order to re-enable these tests.
public class ClueAnalyzerTest {

    private static String dbId = "passage-wiki-2017";
    private QueryEngine queryEngine;

    public void pronominalLATDetection() {

        String clue = "The worse speller of a famous duo in November 1805 he wrote in his journal Ocian in view.";

        ClueAnalyzer clueAnalyzer = new ClueAnalyzer();
        String lat = clueAnalyzer.getAnswerType(clue);

        Assert.assertEquals(lat, "PERSON");
    }

    public void openLATDetection1() {

        String clue = "This European city is famous for its waffles.";

        ClueAnalyzer clueAnalyzer = new ClueAnalyzer();
        String lat = clueAnalyzer.getAnswerType(clue);
        Assert.assertEquals(lat, "European city");
    }

}
