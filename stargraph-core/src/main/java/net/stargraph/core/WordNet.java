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

import edu.smu.tspell.wordnet.*;
import net.stargraph.model.wordnet.PosType;
import net.stargraph.model.wordnet.WNTuple;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.*;

/**
 * Simple Wrapper to read Word Net database.
 * <p>
 * Depends on the installation of the WordNet Database.
 * Some unix distros distributes binary packages, like Ubuntu (packages: wordnet, wordnet-sense-index).
 */
public final class WordNet {

    public WordNet(File wnDatabaseDir) throws FileNotFoundException {
        if (!wnDatabaseDir.exists()) {
            throw new FileNotFoundException(String.format("%s not found", wnDatabaseDir));
        }
        System.setProperty("wordnet.database.dir", wnDatabaseDir.getAbsolutePath());
    }

    public Collection<WNTuple> getHypernyms(String sentence) {
        Set<WNTuple> all = new HashSet<>();
        //TODO: Not the fastest way to compute this. We could try to use stream api to yield tokens instead.
        tokenize(sentence).forEach(t -> all.addAll(getWordHypernyms(t)));
        return all;
    }

    public Collection<WNTuple> getWordHypernymsFromMostCommonNounSense(String word) {
        Set<WNTuple> hypernyms = new HashSet<>();

        WordNetDatabase database = WordNetDatabase.getFileInstance();
        Collection<Synset> synsets = Arrays.asList(database.getSynsets(word));
        for (Synset s : synsets) {
            if (s.getType().equals(SynsetType.NOUN)) {
                if (s instanceof NounSynset) {
                    NounSynset ns = (NounSynset) s;
                    for (NounSynset n : ns.getHypernyms()) {
                        for (String wf : n.getWordForms()) {
                            hypernyms.add(new WNTuple(PosType.fromCode(n.getType().getCode()), wf));
                        }
                    }
                }
            }
            break;
        }

        return hypernyms;
    }

    private Collection<WNTuple> getWordHypernyms(String word) {
        Set<WNTuple> hypernyms = new HashSet<>();

        WordNetDatabase database = WordNetDatabase.getFileInstance();
        Collection<Synset> synsets = Arrays.asList(database.getSynsets(word));

        synsets.stream()
                .filter(s -> s.getType().equals(SynsetType.NOUN) || s.getType().equals(SynsetType.VERB))
                .forEach(s -> {
                    if (s instanceof NounSynset) {
                        NounSynset ns = (NounSynset) s;
                        for (NounSynset n : ns.getHypernyms()) {
                            for (String wf : n.getWordForms()) {
                                hypernyms.add(new WNTuple(PosType.fromCode(n.getType().getCode()), wf));
                            }
                        }
                    } else {
                        VerbSynset ns = (VerbSynset) s;
                        for (Synset n : ns.getHypernyms()) {
                            for (String wf : n.getWordForms()) {
                                hypernyms.add(new WNTuple(PosType.fromCode(n.getType().getCode()), wf));
                            }
                        }
                    }
                });


        return hypernyms;
    }

    public Collection<WNTuple> getHyponyms(String sentence) {
        Set<WNTuple> all = new HashSet<>();
        //TODO: Not the fastest way to compute this. We could try to use stream api to yield tokens instead.
        tokenize(sentence).forEach(t -> all.addAll(getWordHyponyms(t)));
        return all;
    }

    private Set<WNTuple> getWordHyponyms(String word) {
        Set<WNTuple> hyponyms = new HashSet<>();

        WordNetDatabase database = WordNetDatabase.getFileInstance();
        Collection<Synset> synsets = Arrays.asList(database.getSynsets(word));

        synsets.stream()
                .filter(s -> s.getType().equals(SynsetType.NOUN) || s.getType().equals(SynsetType.VERB))
                .forEach(s -> {
                    if (s instanceof NounSynset) {
                        NounSynset ns = (NounSynset) s;
                        for (NounSynset n : ns.getHyponyms()) {
                            for (String wf : n.getWordForms()) {
                                hyponyms.add(new WNTuple(PosType.fromCode(n.getType().getCode()), wf));
                            }
                        }
                    } else {
                        VerbSynset ns = (VerbSynset) s;
                        for (Synset n : ns.getTroponyms()) {
                            for (String wf : n.getWordForms()) {
                                hyponyms.add(new WNTuple(PosType.fromCode(n.getType().getCode()), wf));
                            }
                        }
                    }
                });

        return hyponyms;
    }

    public Set<WNTuple> getSynonyms(String sentence) {
        Set<WNTuple> allSyns = new HashSet<>();
        tokenize(sentence).forEach(t -> allSyns.addAll(getWordSynonyms(t)));
        return allSyns;
    }

    private Collection<WNTuple> getWordSynonyms(String word) {
        Set<WNTuple> syns = new HashSet<>();

        WordNetDatabase database = WordNetDatabase.getFileInstance();
        Collection<Synset> synsets = Arrays.asList(database.getSynsets(word));

        synsets.stream()
                .filter(s -> s.getType().equals(SynsetType.NOUN) || s.getType().equals(SynsetType.VERB))
                .forEach(s -> {
                    for (String wf : s.getWordForms()) {
                        syns.add(new WNTuple(PosType.fromCode(s.getType().getCode()), wf));
                    }
                });

        return syns;
    }

    private static List<String> tokenize(String sentence) {
        List<String> tokens = new ArrayList<>();
        StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(sentence));

        try {
            while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
                if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
                    tokens.add(tokenizer.sval);
                }
            }
            return tokens;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
