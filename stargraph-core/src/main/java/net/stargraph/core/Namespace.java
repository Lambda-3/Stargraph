package net.stargraph.core;

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
import net.stargraph.model.KBId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Namespace extends HashMap<String, String> {
    private static Logger logger = LoggerFactory.getLogger(Namespace.class);
    private static Marker marker = MarkerFactory.getMarker("core");

    public Namespace(String resource) {
        String classPathResource = String.format("%s-namespace.txt", Objects.requireNonNull(resource));
        logger.info(marker, "Namespace resource: {}", classPathResource);

        try (BufferedReader buf = getReader(classPathResource)) {
            buf.lines().forEach(line -> {
                try {
                    String[] s = line.split(" ");
                    put(s[1], s[0] + ":");
                } catch (Exception e) {
                    logger.error(marker, "Error reading {}", line, e);
                }
            });

        } catch (Exception e) {
            throw new StarGraphException("Can't initialize Namespace map", e);
        }
    }

    public String map(String uri) {
        for (Map.Entry<String, String> entry : this.entrySet()) {
            if (uri.startsWith(entry.getKey())) {
                return uri.replace(entry.getKey(), entry.getValue());
            }
        }
        return uri;
    }

    public static Namespace create(Stargraph core, KBId kbId) {
        Config kbConfig = core.getKBConfig(kbId);
        if (kbConfig.hasPath("triple-store.namespace.mapping")) {
            return new Namespace(kbConfig.getString("triple-store.namespace.mapping"));
        }
        logger.warn(marker, "No Namespace mapping defined.");
        return null;
    }

    private BufferedReader getReader(String resource) {
        try {
            InputStream is = ClassLoader.getSystemResourceAsStream(resource);
            BufferedInputStream input = new BufferedInputStream(is);
            return new BufferedReader(new InputStreamReader(input));
        } catch (Exception e) {
            throw new StarGraphException(e);
        }
    }
}
