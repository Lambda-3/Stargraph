package net.stargraph.rest;

import net.stargraph.query.InteractionMode;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class UserResponse implements Serializable {
    private String query;
    private String sparqlQuery;
    private InteractionMode interactionMode;
    private List<EntityEntry> answers;
    private Map<String, List<EntityEntry>> mappings;

    public UserResponse(String query, String sparqlQuery, InteractionMode interactionMode) {
        this.query = Objects.requireNonNull(query);
        this.sparqlQuery = Objects.requireNonNull(sparqlQuery);
        this.interactionMode = Objects.requireNonNull(interactionMode);
    }

    public void setAnswers(List<EntityEntry> entries) {
        this.answers = Objects.requireNonNull(entries);
    }

    public void setMappings(Map<String, List<EntityEntry>> mappings) {
        this.mappings = Objects.requireNonNull(mappings);
    }

    public String getQuery() {
        return query;
    }

    public String getSparqlQuery() {
        return sparqlQuery;
    }

    public InteractionMode getInteractionMode() {
        return interactionMode;
    }

    public List<EntityEntry> getAnswers() {
        return answers;
    }

    public Map<String, List<EntityEntry>> getMappings() {
        return mappings;
    }

    public static class EntityEntry {
        public String id;
        public String value;
        public double score;

        public EntityEntry(String id, String value) {
            this(id, value, 1);
        }

        public EntityEntry(String id, String value, double score) {
            this.id = id;
            this.value = value;
            this.score = score;
        }
    }
}
