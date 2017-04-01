package net.stargraph.core.query.annotator;

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

import net.stargraph.query.Language;
import net.stargraph.StarGraphException;
import net.stargraph.UnsupportedLanguageException;

import java.util.HashSet;

public abstract class PartOfSpeechSet extends HashSet<POSTag> {

    public final POSTag valueOf(String tagName) {
        return this.stream().filter(pos -> pos.getTag().equals(tagName))
                .findFirst().orElseThrow(() -> new StarGraphException("No mapping for '" + tagName + "'"));
    }

    public static PartOfSpeechSet getPOSSet(Language language) {
        switch (language) {
            case EN:
                return EnglishPOSSet.getInstance();
            case DE:
            case PT:
                throw new UnsupportedLanguageException(language);
            default:
                throw new StarGraphException("Unknown Language: '" + language + "'");
        }
    }
}
