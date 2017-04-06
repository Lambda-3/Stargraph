package net.stargraph.rest;

import net.stargraph.query.InteractionMode;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SchemaAgnosticUserResponse extends UserResponse {
    private String sparqlQuery;
    private List<EntityEntry> answers;
    private Map<String, List<EntityEntry>> mappings;

    public SchemaAgnosticUserResponse(String query, InteractionMode interactionMode, String sparqlQuery) {
        super(query, interactionMode);
        this.sparqlQuery = Objects.requireNonNull(sparqlQuery);
    }

    public void setAnswers(List<EntityEntry> entries) {
        this.answers = Objects.requireNonNull(entries);
    }

    public void setMappings(Map<String, List<EntityEntry>> mappings) {
        this.mappings = Objects.requireNonNull(mappings);
    }

    public String getSparqlQuery() {
        return sparqlQuery;
    }

    public List<EntityEntry> getAnswers() {
        return answers;
    }

    public Map<String, List<EntityEntry>> getMappings() {
        return mappings;
    }
}
