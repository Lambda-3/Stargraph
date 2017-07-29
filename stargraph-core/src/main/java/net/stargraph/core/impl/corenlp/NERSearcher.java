package net.stargraph.core.impl.corenlp;

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

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import net.stargraph.core.ner.LinkedNamedEntity;
import net.stargraph.core.ner.NER;
import net.stargraph.core.search.EntitySearcher;
import net.stargraph.model.InstanceEntity;
import net.stargraph.query.Language;
import net.stargraph.rank.ModifiableSearchParams;
import net.stargraph.rank.ParamsBuilder;
import net.stargraph.rank.Scores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.*;
import java.util.stream.Collectors;

public final class NERSearcher implements NER {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Marker marker = MarkerFactory.getMarker("ner");
    private CoreNLPNERClassifier ner;
    private EntitySearcher entitySearcher;
    private String entitySearcherDbId;
    private boolean reverseNameOrder;

    public NERSearcher(Language language, EntitySearcher entitySearcher, String entitySearcherDbId) {
        this.ner = new CoreNLPNERClassifier(Objects.requireNonNull(language));
        this.entitySearcher = Objects.requireNonNull(entitySearcher);
        this.entitySearcherDbId = Objects.requireNonNull(entitySearcherDbId);
        this.reverseNameOrder = false; //TODO: read from configuration, specific for each KB.
    }

    @Override
    public List<LinkedNamedEntity> searchAndLink(String text) {
        logger.debug(marker, "'{}'", text);
        List<LinkedNamedEntity> linked = null;
        long start = System.nanoTime();
        try {
            final List<List<CoreLabel>> sentences = ner.classify(text); //TODO: Improve decoupling, still tied to CoreNLP
            logger.trace(marker, "NER output: {}", sentences);
            linked = postProcessFoundNamedEntities(sentences);
            return linked;
        }
        finally {
            double elapsedInMillis = (System.nanoTime() - start) / 1000_000;
            logger.debug(marker, "Took {}ms, entities: {}, text: '{}'", elapsedInMillis, linked, text);
        }
    }

    private List<LinkedNamedEntity> postProcessFoundNamedEntities(List<List<CoreLabel>> sentences) {
        final List<List<LinkedNamedEntity>> sentenceList = mergeConsecutiveNamedEntities(sentences);

        if (this.reverseNameOrder) {
            sentenceList.forEach(sentence -> {
                sentence.forEach(LinkedNamedEntity::reverseValue);
            });
        }

        if (sentenceList.isEmpty() || (sentenceList.size() == 1 && sentenceList.get(0).isEmpty())) {
            logger.trace(marker, "No Entities detected.");
            return Collections.emptyList();
        }

        return linkNamedEntities(sentenceList);
    }

    /**
     * Receives a list of CoreLabel (from CoreNLP package) and merges two consecutive named entities with
     * the same label into a single one.
     *
     * Example: "Barack/PERSON Obama/PERSON" becomes "Barack Obama/PERSON"
     *
     * @param sentences List of lists of CoreLabels
     * @return List of ScoredNamedEntities where consecutive named entities are combined
     */
    private static List<List<LinkedNamedEntity>> mergeConsecutiveNamedEntities(List<List<CoreLabel>> sentences) {
        final List<List<LinkedNamedEntity>> sentenceList = new ArrayList<>();

        for (List<CoreLabel> sentence : sentences) {

            List<LinkedNamedEntity> namedEntities = new ArrayList<>();
            String previousCat = null;
            LinkedNamedEntity currentNamedEntity = null;

            /*
                A named entity is composed of multiple words, most of the time.
                Two consecutive words belong to one named entity if they have the same label.
                This method does not differentiate two different named entities when they are not
                divided by a different label.
                CoreNLP labels words that are not a named entity with "O", so we remove these from the output.
             */
            for (CoreLabel coreLabel : sentence) {

                String currentWord = coreLabel.originalText();

                String currentCat = coreLabel.get(CoreAnnotations.AnswerAnnotation.class);

                if (currentNamedEntity == null) {
                    currentNamedEntity = new LinkedNamedEntity(currentWord, currentCat, coreLabel.beginPosition(), coreLabel.endPosition());
                } else if (currentCat.equals(previousCat)) {
                    currentNamedEntity.merge(currentNamedEntity.getValue() + " " + currentWord, coreLabel.endPosition());
                } else {
                    namedEntities.add(currentNamedEntity);
                    currentNamedEntity = new LinkedNamedEntity(currentWord, currentCat, coreLabel.beginPosition(), coreLabel.endPosition());
                }

                previousCat = currentCat;
            }

            /* Add last NE when not already added.
               This happens, when the last token in a sentence belongs to a named entity.
             */
            if (!namedEntities.contains(currentNamedEntity)) {
                namedEntities.add(currentNamedEntity);
            }

            // ignore NamedEntities with label "O", they are not NamedEntities
            sentenceList.add(namedEntities
                    .stream()
                    .filter(s -> !s.getCat().equals("O"))
                    .collect(Collectors.toList()));
        }

        return sentenceList.stream().filter(s -> s.size() > 0).collect(Collectors.toList());
    }

    private List<LinkedNamedEntity> linkNamedEntities(List<List<LinkedNamedEntity>> sentenceList) {
        List<LinkedNamedEntity> allNamedEntities = new ArrayList<>();

        for (List<LinkedNamedEntity> p : sentenceList) {
            for (LinkedNamedEntity namedEntity : p) {

	            /*
	            Find reference in previous named entities.
	            -> When found: Use that ID, etc.
	            -> Not found: Search in database.
	             */
                Optional<LinkedNamedEntity> reference = findReference(allNamedEntities, namedEntity.getValue());

                if (!reference.isPresent()) {
                    // no reference in previous NEs
                    tryLink(namedEntity);
                }
                else {
                    if (reference.get().getEntity() != null) {
                        namedEntity.link(reference.get().getEntity(), reference.get().getScore());
                    }
                }

                allNamedEntities.add(namedEntity);
            }
        }

        logger.trace(marker, "Linked {} entities.", allNamedEntities.size());

        return allNamedEntities;
    }

    private void tryLink(LinkedNamedEntity namedEntity) {
        if (!namedEntity.getCat().equalsIgnoreCase("DATE")) {
            //TODO: Limit reduce network latency but can hurt precision in some cases
            ModifiableSearchParams searchParams =
                    ModifiableSearchParams.create(this.entitySearcherDbId).term(namedEntity.getValue()).limit(50);

            logger.info(marker, "Trying to link {}", namedEntity);

            final Scores scores = entitySearcher.instanceSearch(searchParams, ParamsBuilder.levenshtein());

            // Currently, we only care about the highest scored entity.
            if (scores.size() > 0) {
                InstanceEntity instance = (InstanceEntity) scores.get(0).getEntry();
                double score = scores.get(0).getValue();
                namedEntity.link(instance, score);
            }
        }
    }

    private static Optional<LinkedNamedEntity> findReference(List<LinkedNamedEntity> namedEntities, String namedEntity) {
        LinkedNamedEntity found = null;
        for (LinkedNamedEntity sne : namedEntities) {
            if (sne.getValue().contains(namedEntity)) {
                // use the first occurrence of a substring
                found = sne;
                break;
            }
        }
        return Optional.ofNullable(found);
    }

}
