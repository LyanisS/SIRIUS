package edu.ezip.ing1.pds.business.server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.Train;
import edu.ezip.ing1.pds.commons.Request;
import edu.ezip.ing1.pds.commons.Response;

public class TrainService {

    private final static String LoggingLabel = "B u s i n e s s - T r a i n - S e r v e r";
    private final Logger logger = LoggerFactory.getLogger(LoggingLabel);

    private enum Queries {
        SELECT_ALL_TRAINS("SELECT train_id, train_status_id, track_element_id FROM train"),
        INSERT_TRAIN("INSERT INTO trains (train_id, train_status_id, track_element_id) VALUES (?, ?, ?)");

        private final String query;

        private Queries(final String query) {
            this.query = query;
        }
    }

    public static TrainService inst = null;

    public static final TrainService getInstance() {
        if (inst == null) {
            inst = new TrainService();
        }
        return inst;
    }

    private TrainService() {
    }

    public final Response dispatch(final Request request, final Connection connection)
            throws SQLException, IOException {
        Response response = null;

        final Queries queryEnum = Enum.valueOf(Queries.class, request.getRequestOrder());
        switch (queryEnum) {
            case SELECT_ALL_TRAINS:
                response = selectAllTrains(request, connection);
                break;
            case INSERT_TRAIN:
                response = insertTrain(request, connection);
                break;
            default:
                break;
        }
        return response;
    }

    private Response insertTrain(final Request request, final Connection connection) throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Train train = objectMapper.readValue(request.getRequestBody(), Train.class);
        final PreparedStatement stmt = connection.prepareStatement(Queries.INSERT_TRAIN.query, Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, train.getTrainId());
        stmt.setInt(1, train.getTrainStatusId());
        stmt.setInt(2, train.getTrackElementId());
        stmt.executeUpdate();

        final ResultSet res = stmt.getGeneratedKeys();
        if (res.next()) {
            train.setTrainId(res.getInt(1));
        }

        return new Response(request.getRequestId(), objectMapper.writeValueAsString(train));
    }

    private Response selectAllTrains(final Request request, final Connection connection) throws SQLException, JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Statement stmt = connection.createStatement();
        final ResultSet res = stmt.executeQuery(Queries.SELECT_ALL_TRAINS.query);

        StringBuilder trainsJson = new StringBuilder("[");
        boolean first = true;
        while (res.next()) {
            Train train = new Train();
            train.setTrainId(res.getInt("train_id"));
            train.setTrainStatusId(res.getInt("train_status_id"));
            train.setTrackElementId(res.getInt("track_element_id"));
            if (!first) {
                trainsJson.append(",");
            }
            trainsJson.append(objectMapper.writeValueAsString(train));
            first = false;
        }
        trainsJson.append("]");

        return new Response(request.getRequestId(), trainsJson.toString());
    }
}
