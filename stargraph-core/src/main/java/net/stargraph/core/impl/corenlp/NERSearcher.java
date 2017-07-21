package net.stargraph.core.impl.corenlp;

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
        List<LinkedNamedEntity> linked = null;
        long start = System.nanoTime();
        try {
            final List<List<CoreLabel>> sentences = ner.classify(text);
            logger.trace(marker, "NER output: {}", sentences);
            linked = postProcessFoundNamedEntities(sentences);
            return linked;
        }
        finally {
            double elapsedInMillis = (System.nanoTime() - start) / 1000_000;
            logger.debug(marker, "Took {}ms, linked {} entities: '{}'",
                    elapsedInMillis, linked != null ? linked.size() : 0, text);
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
            logger.trace(marker, "No NEs left to be linked.");
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

        return sentenceList;
    }

    private List<LinkedNamedEntity> linkNamedEntities(List<List<LinkedNamedEntity>> sentenceList) {
        List<LinkedNamedEntity> allNamedEntities = new ArrayList<>();

        if (sentenceList.size() > 1) {
            throw new RuntimeException("Expecting a list with 1 list?");
        }

        logger.debug(marker, "Trying to link {}: {}", sentenceList.get(0).size(), sentenceList.get(0));

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
        final Scores scores = entitySearcher.instanceSearch(ModifiableSearchParams.create(this.entitySearcherDbId).term(namedEntity.getValue()), ParamsBuilder.levenshtein());

        // Currently, we only care about the highest scored entity.
        if (scores.size() > 0) {
            InstanceEntity instance = (InstanceEntity) scores.get(0).getEntry();
            double score = scores.get(0).getValue();
            namedEntity.link(instance, score);
        }
    }

    private static Optional<LinkedNamedEntity> findReference(List<LinkedNamedEntity> namedEntities, String namedEntity) {
        LinkedNamedEntity found = null;
        for (LinkedNamedEntity sne : namedEntities) {
            if (sne.getValue().contains(namedEntity)) {
                // use the first occurence of a substring
                found = sne;
                break;
            }
        }
        return Optional.ofNullable(found);
    }

}
