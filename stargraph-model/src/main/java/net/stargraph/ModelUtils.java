package net.stargraph;

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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import net.stargraph.data.Indexable;
import net.stargraph.data.processor.Holder;
import net.stargraph.model.Fact;
import net.stargraph.model.ResourceEntity;
import net.stargraph.model.KBId;
import net.stargraph.model.PropertyEntity;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities used within the sys core.
 */
public final class ModelUtils {
    private static final Pattern pathFragmentPattern = Pattern.compile("^(\\w+:)(\\w+/)+(\\w+)$");
    private static final String RE_CAMELCASE_OR_UNDERSCORE = "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])|_";

    public static String toStr(Config cfg) {
        return cfg == null ? null : cfg.root().render(ConfigRenderOptions.concise().setFormatted(true));
    }

    public static String extractLabel(String uriStr) {
        return extractLabel(getNamespace(uriStr), uriStr, false);
    }

    public static String extractLabel(String uriStr, boolean splitOnCaseChange) {
        return extractLabel(getNamespace(uriStr), uriStr, splitOnCaseChange);
    }

    public static String extractLabel(String namespace, String uriStr) {
        return extractLabel(namespace, uriStr, false);
    }

    public static String extractLabel(String namespace, String uriStr, boolean splitOnCaseChange) {
        if (namespace == null || uriStr == null || namespace.isEmpty() || uriStr.isEmpty()) {
            throw new IllegalArgumentException("NS nor URI can be null or empty.");
        }

        if (!uriStr.startsWith(namespace)) {
            throw new IllegalArgumentException("NS is not in the URI");
        }

        // Result from degenerated resource like <http://example.com>
        if (namespace.equals("http://") || namespace.equals("https://")) {
            return uriStr;
        }

        // Result from degenerated resource like <http://example.com/>
        if (namespace.equals(uriStr)) {
            return uriStr;
        }

        //Replacements based on http://wiki.dbpedia.org/uri-encoding
        //Also tests against the dump to verify the correct mappings


        String remaining;

        // This is to handle with resource having a path as value Like in dbt:Editnotices/Page/Barack_Obama.
        // The extracted label is not Barack_Obama but the whole path.
        // SplitIRI.namespace is failing in this case detecting  dbt:Editnotices/Page/ as the NS
        Matcher matcher = pathFragmentPattern.matcher(uriStr);
        if (matcher.matches()) {
            return uriStr.replace(matcher.group(1), "");
        }
        else {
            remaining = uriStr.replace(namespace, "");
        }

        // What is that? <http://dbpedia.org/property/_>
        if (remaining.length() == 1) {
            return remaining;
        }

        remaining = remaining.replaceAll("_", " ");
        remaining = remaining.replace("%3F", "?");
        remaining = remaining.replace("%60", "`");
        remaining = remaining.replace("%22", "\\\"");
        remaining = remaining.replace("%25", "%");
        remaining = remaining.replace("%5C", "\\\\");
        remaining = remaining.replace("%5E", "^");

        if (!splitOnCaseChange)
            return remaining;

        return split(remaining);
    }

    public static byte[] createHashId(Serializable data) {
        if (data == null) {
            throw new IllegalArgumentException("data can't be null");
        }

        try (ByteArrayOutputStream b = new ByteArrayOutputStream()) {
            try (ObjectOutputStream o = new ObjectOutputStream(b)) {
                o.writeObject(data);
            }
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            return digest.digest(b.toByteArray());
        } catch (Exception e) {
            throw new StarGraphException("Fail to create new hash id.", e);
        }
    }

    public static ResourceEntity createResource(String uri) {
        return new ResourceEntity(uri, extractLabel(uri, true));
    }

    public static PropertyEntity createProperty(String uri) {
        return new PropertyEntity(uri, extractLabel(uri, true));
    }

    public static Fact createFact(KBId kbId, String s, String p, String o) {
        return new Fact(kbId, ModelUtils.createResource(s), ModelUtils.createProperty(p), ModelUtils.createResource(o));
    }

    public static Holder createWrappedFact(KBId kbId, String s, String p, String o) {
        Fact fact = ModelUtils.createFact(kbId, s, p, o);
        return new Indexable(fact, kbId);
    }

    public static Holder createWrappedProperty(KBId kbId, String p) {
        PropertyEntity propertyEntity = ModelUtils.createProperty(p);
        return new Indexable(propertyEntity, kbId);
    }

    private static String getNamespace(String uriStr) {
        if (uriStr.startsWith("http://")) {
            return SplitIRI.namespace(uriStr);
        }
        else {
            return uriStr.split(":")[0] + ':'; //NS already resolved.
        }
    }

    private static String split(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }

        StringJoiner joiner = new StringJoiner(" ");
        for (String word : s.split(RE_CAMELCASE_OR_UNDERSCORE)) {
            if (!word.isEmpty()) {
                joiner.add(word.trim());
            }
        }
        return joiner.toString();
    }

}
