package net.stargraph.core.processors;

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

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.typesafe.config.Config;
import net.stargraph.data.processor.BaseProcessor;
import net.stargraph.data.processor.Hashable;
import net.stargraph.data.processor.Holder;
import net.stargraph.data.processor.ProcessorException;

public final class SinkDuplicateProcessor extends BaseProcessor {
    public static String name = "sink-duplicate";
    private BloomFilter<byte[]> bloomFilter;

    public SinkDuplicateProcessor(Config config) {
        super(config);
        //TODO: should be in configuration, assuming worst case scenario with acceptable loss.
        //In the near future we must treat the losses.
        bloomFilter = BloomFilter.create(Funnels.byteArrayFunnel(), 100000000, 0.02);
    }

    @Override
    public void doRun(Holder holder) throws ProcessorException {
        checkFilter(holder);
    }

    @Override
    public String getName() {
        return name;
    }

    private void checkFilter(Holder holder) {
        Hashable o = (Hashable) holder.get();
        byte[] hashId = o.hash();
        holder.setSink(bloomFilter.mightContain(hashId));
        bloomFilter.put(hashId);
    }
}
