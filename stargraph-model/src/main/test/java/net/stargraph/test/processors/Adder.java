package net.stargraph.test.processors;

import net.stargraph.data.processor.Holder;
import net.stargraph.data.processor.Processor;
import net.stargraph.data.processor.ProcessorException;

public final class Adder implements Processor<Number> {

    private int value;

    public Adder(int v) {
        this.value = v;
    }

    @Override
    public void run(Holder<Number> holder) throws ProcessorException {
        holder.set(holder.get().intValue() + value);
    }
}
