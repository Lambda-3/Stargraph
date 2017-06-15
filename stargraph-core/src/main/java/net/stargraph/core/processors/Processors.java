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

import com.typesafe.config.Config;
import net.stargraph.StarGraphException;
import net.stargraph.data.processor.Processor;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public final class Processors {

    private static Map<String, Class<? extends Processor>> registered;

    static {
        registered = new HashMap<String, Class<? extends Processor>>() {{
            put(SinkDuplicateProcessor.name, SinkDuplicateProcessor.class);
            put(NamespaceProcessor.name, NamespaceProcessor.class);
            put(EntityClassifierProcessor.name, EntityClassifierProcessor.class);
            put(WordNetProcessor.name, WordNetProcessor.class);
            put(WordNetProcessor.name, WordNetProcessor.class);
            put(RegExFilterProcessor.name, RegExFilterProcessor.class);
            put(StopPropertyFilterProcessor.name, StopPropertyFilterProcessor.class);
            put(LengthFilterProcessor.name, LengthFilterProcessor.class);
            put(CoreferenceResolutionProcessor.name, CoreferenceResolutionProcessor.class);
        }};
    }


    public static Processor create(Config config) {
        if (config == null) {
            throw new IllegalArgumentException("config is required.");
        }

        if (config.root().entrySet().size() != 1) {
            throw new IllegalStateException("This configuration must have only one key value!");
        }

        //Expected name of the registered processor
        final String name = config.root().keySet().iterator().next();

        Class<? extends Processor> c = registered.get(name);

        if (c == null) {
            throw new StarGraphException("No processor named '" + name + "'");
        }

        try {
            Constructor<? extends Processor> constructor = c.getConstructor(Config.class);
            return constructor.newInstance(config);
        } catch (Exception e) {
            throw new StarGraphException("Fail to create new processor.", e);
        }
    }
}
