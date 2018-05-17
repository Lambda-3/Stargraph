package net.stargraph.test;

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

import net.stargraph.data.DataProvider;
import net.stargraph.data.DataSource;
import net.stargraph.data.Indexable;
import net.stargraph.model.KBId;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataProviderTest {


    @Test
    public void fetchLastTest() {
        List<Indexable> data = wrap(Arrays.asList("data1", "data2", "data3"));
        DataProvider<Indexable> provider = new DataProvider<>(
                new DataSource<Indexable>() {
                    @Override
                    public Iterator<Indexable> createIterator() {
                        return data.iterator();
                    }
                }
        );
        Indexable last = provider.getMergedDataSource().getStream().skip(2).findFirst().orElseThrow(() -> new RuntimeException("fail"));
        Assert.assertEquals(last.get(), "data3");
    }

    @Test
    public void fetchAllTest() {
        List<Indexable> data = wrap(Arrays.asList("data1", "data2", "data3"));
        DataProvider<Indexable> provider = new DataProvider<>(
                new DataSource<Indexable>() {
                    @Override
                    public Iterator<Indexable> createIterator() {
                        return data.iterator();
                    }
                }
        );
        Stream<Indexable> stream = provider.getMergedDataSource().getStream();
        List<Indexable> collected = stream.collect(Collectors.toList());
        Assert.assertEquals(collected, data);
    }

    @Test
    public void restartTest() {
        List<Indexable> data = wrap(Arrays.asList("data1", "data2", "data3"));
        DataProvider<Indexable> provider = new DataProvider<>(
                new DataSource<Indexable>() {
                    @Override
                    public Iterator<Indexable> createIterator() {
                        return data.iterator();
                    }
                }
        );
        Stream<Indexable> stream = provider.getMergedDataSource().getStream();
        List<Indexable> collected = stream.collect(Collectors.toList());
        Assert.assertEquals(collected, data);

        provider = new DataProvider<>(
                new DataSource<Indexable>() {
                    @Override
                    public Iterator<Indexable> createIterator() {
                        return data.iterator();
                    }
                }
        );
        stream = provider.getMergedDataSource().getStream();
        collected = stream.collect(Collectors.toList());
        Assert.assertEquals(collected, data);
    }


    private static List<Indexable> wrap(List<Serializable> data) {
        return data.stream().map(s -> new Indexable(s, KBId.of("testdb", "test"))).collect(Collectors.toList());
    }

}
