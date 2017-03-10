package net.stargraph.model.wordnet;

/*-
 * ==========================License-Start=============================
 * stargraph-model
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

public final class WNTuple {

    private PosType posType;
    private String word;

    public WNTuple(PosType pos, String w) {
        if (pos == null || w == null || w.isEmpty()) {
            throw new IllegalArgumentException();
        }

        this.posType = pos;
        this.word = w;
    }

    public PosType getPosType() {
        return posType;
    }

    public String getWord() {
        return word;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WNTuple)) return false;

        WNTuple wnTuple = (WNTuple) o;

        if (getPosType() != wnTuple.getPosType()) return false;
        return !(getWord() != null ? !getWord().equals(wnTuple.getWord()) : wnTuple.getWord() != null);

    }

    @Override
    public int hashCode() {
        int result = getPosType() != null ? getPosType().hashCode() : 0;
        result = 31 * result + (getWord() != null ? getWord().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "WNTuple{" +
                "posType=" + posType +
                ", word='" + word + '\'' +
                '}';
    }
}
