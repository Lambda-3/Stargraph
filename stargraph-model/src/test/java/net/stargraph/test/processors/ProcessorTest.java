package net.stargraph.test.processors;

import net.stargraph.data.processor.ProcessorChain;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

public final class ProcessorTest {

    @Test
    public void simpleChainTest() {
        ProcessorChain chain = new ProcessorChain(Arrays.asList(new Adder(1), new Multiplier(2), new Adder(4)));
        NumberHolder ctx = new NumberHolder();
        ctx.set(2); // initialValue
        chain.run(ctx); // ((initialValue + 1) * 2) + 4 = 10
        Assert.assertEquals((int) ctx.get(), 10);
    }
}
