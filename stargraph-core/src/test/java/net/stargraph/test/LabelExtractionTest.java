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

import net.stargraph.ModelUtils;
import net.stargraph.core.Namespace;
import org.testng.Assert;
import org.testng.annotations.Test;

import static net.stargraph.ModelUtils.extractLabel;

/**
 * Among other tests this runs the label extraction against the dump file dbpedia-2016-labels_en.txt.bz2.
 * This dump is based on the original labels dump from dbpedia. It was preprocessed to make it smaller
 * (a shorter namespace (http://r.co) and all predicates were dropped). In a future version of this test
 * we can use the original version downloaded directly from the dbpedia site.
 */
public final class LabelExtractionTest {

    private Namespace ns = Namespace.createDefault();

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void namespaceFailTest() {
        extractLabel("http://r.co", "http://dbpedia.com/AnyLabel");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void emptyFail1Test() {
        extractLabel("", "http://dbpedia.com/AnyLabel");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void emptyFail2Test() {
        extractLabel(null, "http://dbpedia.com/AnyLabel");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void namespaceFail3Test() {
        extractLabel("http://r.co", "");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void namespaceFail4Test() {
        extractLabel("http://r.co", null);
    }

    @Test
    public void shortLabelTest() {
        Assert.assertEquals(extractLabel("co:", "co:x"), "x");
    }

    @Test
    public void shortLabel2Test() {
        Assert.assertEquals(extractLabel("co:xYz", true), "x Yz");
    }

    @Test
    public void mapAndLabelExtractionTest() {
        String uri = ns.shrinkURI("http://dbpedia.org/resource/Template:Editnotices/Page/Barack_Obama");
        Assert.assertEquals(ModelUtils.extractLabel(uri), "Editnotices/Page/Barack_Obama");
    }

    @Test
    public void pathOnLabelExtractionTest() {
        String uri = ns.shrinkURI("http://dbpedia.org/resource/Mount_Wellington,_BC/Canada");
        Assert.assertEquals(ModelUtils.extractLabel(uri), "Mount Wellington, BC/Canada");
    }

    @Test
    public void labelWithParenthesisExtractionTest() {
        String uri = ns.shrinkURI("http://dbpedia.org/resource/Project_Agreements_(Project_Labor_Agreements_–_Canada)");
        Assert.assertEquals(ModelUtils.extractLabel(uri), "Project Agreements (Project Labor Agreements – Canada)");
    }

    @Test
    public void doubleSlashTest() {
        Assert.assertEquals(extractLabel("co:", "co:x/y/z"), "x/y/z");
        Assert.assertEquals(extractLabel("http://stargraph.net/", "http://stargraph.net/x/y/z"), "x/y/z");
        Assert.assertEquals(extractLabel("http://stargraph.net/", "http://stargraph.net/x/y_z"), "x/y z");
    }

    @Test
    public void camelCaseTest() {
        Assert.assertEquals(extractLabel("co:", "co:AbrahamLincoln", true), "Abraham Lincoln");
        Assert.assertEquals(extractLabel("co:", "co:AbrahamLincoln", false), "AbrahamLincoln");
    }

    @Test
    public void sampleTest() {
        Assert.assertEquals(extractLabel("http://dbpedia.org/resource/",
                "http://dbpedia.org/resource/Democratic_Party_(United_States)"), "Democratic Party (United States)");
    }

}
