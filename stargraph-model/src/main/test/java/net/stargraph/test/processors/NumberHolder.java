package net.stargraph.test.processors;

import net.stargraph.data.processor.Holder;
import net.stargraph.model.KBId;

import java.io.Serializable;

public class NumberHolder implements Holder<Number>, Serializable {

    private Number data;
    private boolean sink;

    @Override
    public Number get() {
        return data;
    }

    @Override
    public void set(Number data) {
        this.data = data;
    }

    @Override
    public void setSink(boolean sink) {
        this.sink = sink;
    }

    @Override
    public boolean isSinkable() {
        return sink;
    }

    @Override
    public KBId getKBId() {
        return KBId.of("any", "thing");
    }
}
