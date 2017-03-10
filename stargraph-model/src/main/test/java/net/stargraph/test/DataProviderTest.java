package net.stargraph.test;

import net.stargraph.data.DataProvider;
import net.stargraph.data.Indexable;
import net.stargraph.model.KBId;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataProviderTest {


    @Test
    public void fetchLastTest() {
        List<Indexable> data = wrap(Arrays.asList("data1", "data2", "data3"));
        DataProvider<Indexable> provider = new DataProvider<>(data.iterator());
        Indexable last = provider.getStream().skip(2).findFirst().orElseThrow(() -> new RuntimeException("fail"));
        Assert.assertEquals(last.get(), "data3");
    }

    @Test
    public void fetchAllTest() {
        List<Indexable> data = wrap(Arrays.asList("data1", "data2", "data3"));
        DataProvider<Indexable> provider = new DataProvider<>(data.iterator());
        Stream<Indexable> stream = provider.getStream();
        List<Indexable> collected = stream.collect(Collectors.toList());
        Assert.assertEquals(collected, data);
    }

    @Test
    public void restartTest() {
        List<Indexable> data = wrap(Arrays.asList("data1", "data2", "data3"));
        DataProvider<Indexable> provider = new DataProvider<>(data.iterator());
        Stream<Indexable> stream = provider.getStream();
        List<Indexable> collected = stream.collect(Collectors.toList());
        Assert.assertEquals(collected, data);

        provider = new DataProvider<>(data.iterator());
        stream = provider.getStream();
        collected = stream.collect(Collectors.toList());
        Assert.assertEquals(collected, data);
    }


    private static List<Indexable> wrap(List<Serializable> data) {
        return data.stream().map(s -> new Indexable(s, KBId.of("testdb", "test"))).collect(Collectors.toList());
    }

}
