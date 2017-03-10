package net.stargraph.test.processors;

import net.stargraph.data.processor.Holder;
import net.stargraph.data.processor.Processor;
import net.stargraph.data.processor.ProcessorException;

public final class Multiplier implements Processor<Number> {

    private int value;

    public Multiplier(int v) {
        this.value = v;
    }

    @Override
    public void run(Holder<Number> holder) throws ProcessorException {
        holder.set(holder.get().intValue() * value);
    }
}
