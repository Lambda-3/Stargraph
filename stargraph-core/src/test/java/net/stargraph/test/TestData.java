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

import java.io.Serializable;
import java.util.Objects;

/**
 * Test expected model. Encapsulates a String.
 */
public final class TestData implements Serializable {

    public boolean failOnIndexer = false;
    public boolean failOnProvider = false;
    public String text;

    public TestData(String text) {
        this(false, false, text);
    }

    public TestData(boolean failOnIndexer, boolean failOnProvider, String text) {
        this.failOnIndexer = failOnIndexer;
        this.failOnProvider = failOnProvider;
        this.text = text;
    }


    @Override
    public String toString() {
        return "TestData{" +
                "failOnIndexer=" + failOnIndexer +
                ", failOnProvider=" + failOnProvider +
                ", text='" + text + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestData testData = (TestData) o;
        return failOnIndexer == testData.failOnIndexer &&
                failOnProvider == testData.failOnProvider &&
                Objects.equals(text, testData.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(failOnIndexer, failOnProvider, text);
    }
}
