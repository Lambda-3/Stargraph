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

import static net.stargraph.rank.Threshold.ThresholdType.*;


/**
 * Represents the different kinds of Threshold filtering.
 */
public final class Threshold {

    public enum ThresholdType {
        MAX, MIN, AUTO
    }

    public ThresholdType type;
    public double value;

    private Threshold(ThresholdType type, double v) {
        this.type = type;
        this.value = v;
    }

    public static Threshold max(double v) {
        return new Threshold(MAX, v);
    }

    public static Threshold min(double v) {
        return new Threshold(MIN, v);
    }

    public static Threshold auto() {
        return new Threshold(AUTO, 0);
    }

    @Override
    public String toString() {
        if (type != AUTO) {
            return "Threshold{" + type + ", " + value + '}';
        }
        return "Threshold{" + type + ", _ }";
    }
}
