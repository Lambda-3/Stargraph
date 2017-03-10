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

import java.util.*;
import java.util.stream.Collectors;

public final class Sorter {

    public static <K, V extends Comparable> Map<K, V> reverse(Map<K, V> map, int limit) {
        return sortByValues(map, limit, true);
    }

    public static <K, V extends Comparable> Map<K, V> reverse(Map<K, V> map) {
        return sortByValues(map, -1, true);
    }

    public static <K, V extends Comparable> Map<K, V> sort(Map<K, V> map) {
        return sortByValues(map, -1, false);
    }

    public static <K, V extends Comparable> Map<K, V> sortByValues(Map<K, V> map, int limit, boolean reversed) {
        List<Map.Entry<K, V>> entries = new LinkedList<>(map.entrySet());

        Comparator<Map.Entry<K, V>> comparator
                = (o1, o2) -> o1.getValue().compareTo(o2.getValue());

        if (reversed) {
            comparator = comparator.reversed();
        }

        if (limit > 0) {
            entries = entries.stream()
                    .sorted(comparator)
                    .limit(limit).collect(Collectors.toList());
        } else {
            entries = entries.stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());
        }

        Map<K, V> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : entries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
}
