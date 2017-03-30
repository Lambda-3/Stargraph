package net.stargraph.server;

import net.stargraph.core.Stargraph;
import net.stargraph.core.query.AnswerSet;
import net.stargraph.core.query.QueryEngine;
import net.stargraph.core.query.QueryResponse;
import net.stargraph.rest.QueryResource;
import net.stargraph.rest.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class QueryResourceImpl implements QueryResource {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Marker marker = MarkerFactory.getMarker("server");
    private Stargraph core;
    private Map<String, QueryEngine> engines;

    public QueryResourceImpl(Stargraph core) {
        this.core = Objects.requireNonNull(core);
        this.engines = new ConcurrentHashMap<>();
    }

    @Override
    public Response query(String id, String q) {
        try {
            if (core.hasKB(id)) {
                QueryEngine engine = engines.computeIfAbsent(id, (k) -> new QueryEngine(k, core));
                QueryResponse queryResponse = engine.query(q);
                return Response.status(Response.Status.OK).entity(buildUserResponse(queryResponse)).build();
            }
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        catch (Exception e) {
            logger.error(marker, "Query execution failed: '{}' on '{}'", q, id, e);
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    public UserResponse buildUserResponse(QueryResponse queryResponse) {

        if (queryResponse instanceof AnswerSet) {
            AnswerSet answerSet = (AnswerSet) queryResponse;
            UserResponse response =
                    new UserResponse(answerSet.getUserQuery(), answerSet.getSparqlQuery(), answerSet.getInteractionMode());

            List<UserResponse.EntityEntry> answers = answerSet.getShortAnswer().stream()
                    .map(a -> new UserResponse.EntityEntry(a.getId(), a.getValue())).collect(Collectors.toList());

            response.setAnswers(answers);

            Map<String, List<UserResponse.EntityEntry>> mappings = new HashMap<>();
            answerSet.getMappings().forEach((modelBinding, scoreList) -> {
                List<UserResponse.EntityEntry> entries = scoreList.stream()
                        .map(s -> new UserResponse.EntityEntry(s.getRankableView().getId(),
                                s.getRankableView().getValue(), s.getValue()))
                        .collect(Collectors.toList());
                mappings.computeIfAbsent(modelBinding.getTerm(), (term) -> new ArrayList<>()).addAll(entries);
            });

            response.setMappings(mappings);

            return response;
        }

        return null;
    }
}
