package net.stargraph.core.query;

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

import net.stargraph.StarGraphException;
import net.stargraph.core.query.nli.DataModelBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TriplePattern {
    private String pattern;


    public TriplePattern(String pattern) {
        this.pattern = Objects.requireNonNull(pattern);
    }

    public String getPattern() {
        return pattern;
    }

    public List<DataModelBinding> map(List<DataModelBinding> bindingList) {
        List<DataModelBinding> mapped = new ArrayList<>();

        for (String s : pattern.split("\\s")) {
            if (!s.startsWith("?VAR") && !s.startsWith("TYPE")) {
                mapped.add(bindingList.stream()
                        .filter(b -> b.getPlaceHolder().equals(s))
                        .findAny().orElseThrow(() -> new StarGraphException("Unmapped placeholder '" + s + "'")));
            }
        }

        return mapped;
    }

    @Override
    public String toString() {
        return "Triple{" +
                "pattern='" + pattern + '\'' +
                '}';
    }
}
