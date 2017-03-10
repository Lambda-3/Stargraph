package net.stargraph.rank;

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

import java.io.Serializable;
import java.util.Objects;

public class Score implements Comparable<Score> {

    private Serializable entry;
    private double score;

    public Score(Serializable entry, double score) {
        if (entry == null) {
            throw new IllegalArgumentException("Entry can't null.");
        }
        this.entry = entry;
        this.score = score;
    }

    public Serializable getEntry() {
        return entry;
    }

    public Rankable getRankableView() {
        return (Rankable) entry; // Yes we know that not all scored entry is Rankable aware
    }

    public double getValue() {
        return score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Score that = (Score) o;
        return Double.compare(that.getValue(), getValue()) == 0 &&
                Objects.equals(getEntry(), that.getEntry());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEntry(), getValue());
    }

    @Override
    public String toString() {
        return "Score{" + entry + ", " + score + "}";
    }

    @Override
    public int compareTo(Score o) {
        return Double.compare(score, o.score);
    }
}
