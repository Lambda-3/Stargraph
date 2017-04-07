package net.stargraph.core.processors;

import com.typesafe.config.Config;
import net.stargraph.data.processor.BaseProcessor;
import net.stargraph.data.processor.Holder;
import net.stargraph.data.processor.ProcessorException;
import net.stargraph.model.Fact;
import net.stargraph.model.InstanceEntity;
import net.stargraph.model.PropertyEntity;

import java.io.Serializable;

/**
 * Can be placed in the workflow to filter out any triple that the value does not satisfy
 * the configured length conditions. Example that should be filtered: http://dbpedia.org/property/x.
 */
public final class LengthFilterProcessor extends BaseProcessor {
    public static String name = "length-filter";
    private int sMin, sMax = 0;
    private int pMin, pMax = 0;
    private int oMin, oMax = 0;

    public LengthFilterProcessor(Config config) {
        super(config);
        if (!getConfig().getIsNull("s")) {
            sMin = getConfig().getInt("s.min");
            sMax = getConfig().getInt("s.max");
        }
        if (!getConfig().getIsNull("p")) {
            pMin = getConfig().getInt("p.min");
            pMax = getConfig().getInt("p.max");
        }
        if (!getConfig().getIsNull("o")) {
            oMin = getConfig().getInt("o.min");
            oMax = getConfig().getInt("o.max");
        }
    }

    @Override
    public void doRun(Holder<Serializable> holder) throws ProcessorException {
        Serializable entry = holder.get();

        if (entry instanceof InstanceEntity) {
            String value = ((InstanceEntity) entry).getValue();
            holder.setSink(!inRange(value.length(), sMin, sMax));
        }
        else if (entry instanceof PropertyEntity) {
            String value = ((PropertyEntity) entry).getValue();
            holder.setSink(!inRange(value.length(), pMin, pMax));
        }
        else if (entry instanceof Fact) {
            Fact fact = (Fact) entry;
            if (fact.getSubject() instanceof InstanceEntity) {
                String value = ((InstanceEntity) fact.getSubject()).getValue();
                holder.setSink(!inRange(value.length(), sMin, sMax));
            }

            if (!holder.isSinkable()) {
                String value = fact.getPredicate().getValue();
                holder.setSink(!inRange(value.length(), pMin, pMax));
            }

            if (!holder.isSinkable()) {
                String value = fact.getObject().getValue();
                holder.setSink(!inRange(value.length(), oMin, oMax));
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    private static boolean inRange(int value, int min, int max) {
        return min <= 0 || max <= 0 || value >= min && value <= max;
    }
}
