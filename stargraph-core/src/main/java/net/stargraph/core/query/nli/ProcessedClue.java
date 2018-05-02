package net.stargraph.core.query.nli;

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

public class ProcessedClue {

    private String text;
    private String posPattern;

    public ProcessedClue() {

    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPosPattern() {
        return posPattern;
    }

    public void setPosPattern(String posPattern) {
        this.posPattern = posPattern;
    }

    public long getWordIndex(String word){

        long i = 0;
        for(String elem : text.toLowerCase().split(" ")) {
            i++;
            if(elem.equals(word))
                return i;
        }

        return -1;
    }

    public long getPOSIndex(String pos){

        long i = 0;
        for(String elem : posPattern.split(" ")) {
            i++;
            if(elem.equals(pos))
                return i;
        }

        return -1;
    }

    public String getTextByIndexRange(long start, long end){

        String out = "";
        long i = 0;
        for(String elem : text.split(" ")) {
            if(i >= start && i <= end)
                out += elem + " ";
            i++;
        }
        return out.trim();
    }

    public String getWordByIndex(long index){

        String out = "";
        long i = 0;
        for(String elem : text.split(" ")) {
            if(i == index)
                return elem;
            i++;
        }
        return "";
    }

    public String getPOSByIndex(long index){

        String out = "";
        long i = 0;
        for(String elem : posPattern.split(" ")) {
            if(i == index)
                return elem;
            i++;
        }
        return "";
    }
}
