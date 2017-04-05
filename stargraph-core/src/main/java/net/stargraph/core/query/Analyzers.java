package net.stargraph.core.query;

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
import net.stargraph.core.query.annotator.Annotator;
import net.stargraph.core.query.annotator.AnnotatorFactory;
import net.stargraph.core.query.nli.QuestionAnalyzer;
import net.stargraph.query.Language;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;

public final class Analyzers {
    private Rules rules;
    private Annotator annotator;
    private ConcurrentHashMap<Language, QuestionAnalyzer> questionAnalyzers;

    public Analyzers(Config config) {
        this.rules = new Rules(config);
        AnnotatorFactory factory = createAnnotatorFactory(config);
        this.annotator = factory.create();
        this.questionAnalyzers = new ConcurrentHashMap<>();
    }

    public QuestionAnalyzer getQuestionAnalyzer(Language language) {
        return questionAnalyzers.computeIfAbsent(language, lang -> new QuestionAnalyzer(lang, annotator, rules));
    }

    public static AnnotatorFactory createAnnotatorFactory(Config config) {
        try {
            String className = config.getString("annotator.factory.class");
            Class<?> providerClazz = Class.forName(className);
            Constructor<?> constructor = providerClazz.getConstructors()[0];
            return (AnnotatorFactory) constructor.newInstance(config);
        } catch (Exception e) {
            throw new StarGraphException("Can't initialize indexers.", e);
        }
    }

}
