package edu.ezip.ing1.pds.business.server;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.Alert;
import edu.ezip.ing1.pds.business.dto.AlertGravity;
import edu.ezip.ing1.pds.business.dto.Alerts;
import edu.ezip.ing1.pds.business.dto.Schedule;
import edu.ezip.ing1.pds.business.dto.Schedules;
import edu.ezip.ing1.pds.business.dto.Station;
import edu.ezip.ing1.pds.business.dto.Stations;
import edu.ezip.ing1.pds.business.dto.Train;
import edu.ezip.ing1.pds.business.dto.Trains;
import edu.ezip.ing1.pds.business.dto.Trip;
import edu.ezip.ing1.pds.commons.Request;
import edu.ezip.ing1.pds.commons.Response;

public class XMartCityService {

    private final static String LoggingLabel = "B u s i n e s s - S e r v e r";
    private final Logger logger = LoggerFactory.getLogger(LoggingLabel);

    private enum Queries {
        SELECT_ALL_TRAINS("SELECT train_id FROM train"),
        INSERT_TRAIN("INSERT INTO train () VALUES ();"),
        DELETE_TRAIN("DELETE FROM train WHERE train_id = ?;"),
        SELECT_ALL_SCHEDULES("SELECT schedule_id, schedule_time_arrival, schedule_time_departure, station_name, trip_id, train_id FROM schedule JOIN station USING (station_name) JOIN trip USING (trip_id) JOIN train USING (train_id);"),
        INSERT_SCHEDULE("INSERT INTO schedule (schedule_time_arrival, schedule_time_departure, station_name, trip_id) VALUES (?, ?, ?, ?);"),
        UPDATE_SCHEDULE("UPDATE schedule SET schedule_time_arrival = ?, schedule_time_departure = ?, station_name = ? WHERE schedule_id = ?;"),
        DELETE_SCHEDULE("DELETE FROM schedule WHERE schedule_id = ?;"),
        SELECT_ALL_ALERTS("SELECT alert_id, alert_message, alert_time, alert_duration, alert_gravity_type, train_id FROM alert;"),
        INSERT_ALERT("INSERT INTO alert (alert_message, alert_time, alert_duration, alert_gravity_type, train_id) VALUES (?, ?, ?, ?, ?);"),
        DELETE_ALERT("DELETE FROM alert WHERE alert_id = ?"),
        SELECT_ALL_STATIONS("SELECT station_name FROM station ORDER BY station_sort");

        private final String query;

        private Queries(final String query) {
            this.query = query;
        }
    }

    public static XMartCityService inst = null;

    public static XMartCityService getInstance() {
        if (inst == null) {
            inst = new XMartCityService();
        }
        return inst;
    }

    private XMartCityService() {
    }

    public final Response dispatch(final Request request, final Connection connection)
            throws InvocationTargetException, IllegalAccessException, SQLException, IOException {
        Response response = null;

        final Queries queryEnum = Enum.valueOf(Queries.class, request.getRequestOrder());
        switch (queryEnum) {
            case SELECT_ALL_TRAINS:
                response = SelectAllTrains(request, connection);
                break;
            case INSERT_TRAIN:
                response = InsertTrain(request, connection);
                break;
            case DELETE_TRAIN:
                response = DeleteTrain(request, connection);
                break;
            case SELECT_ALL_SCHEDULES:
                response = SelectAllSchedules(request, connection);
                break;
            case UPDATE_SCHEDULE:
                response = UpdateSchedule(request, connection);
                break;
            case INSERT_SCHEDULE:
                response = InsertSchedule(request, connection);
                break;
            case DELETE_SCHEDULE:
                response = DeleteSchedule(request, connection);
                break;
            case SELECT_ALL_ALERTS:
                response = SelectAllAlerts(request, connection);
                break;
            case INSERT_ALERT:
                response = InsertAlert(request, connection);
                break;
            case DELETE_ALERT:
                response = DeleteAlert(request, connection);
                break;
            case SELECT_ALL_STATIONS:
                response = SelectAllStations(request, connection);
                break;
            default:
                break;
        }

        return response;
    }

    private Response InsertTrain(final Request request, final Connection connection) throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Train train = objectMapper.readValue(request.getRequestBody(), Train.class);

        try {
            final PreparedStatement stmt = connection.prepareStatement(Queries.INSERT_TRAIN.query, Statement.RETURN_GENERATED_KEYS);
            stmt.executeUpdate();
            ResultSet res = stmt.getGeneratedKeys();
            if (res.next()) train.setId(res.getInt(1));

            return new Response(request.getRequestId(), objectMapper.writeValueAsString(train));
        } catch (SQLException e) {
            logger.error("SQL error inserting train: {}", e.getMessage());
            throw new SQLException("Erreur SQL lors de l'insertion du train: " + e.getMessage(), e);
        }
    }

    private Response SelectAllTrains(final Request request, final Connection connection)
            throws SQLException, JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Statement stmt = connection.createStatement();
        final ResultSet res = stmt.executeQuery(Queries.SELECT_ALL_TRAINS.query);
        Trains trains = new Trains();

        try {
            while (res.next()) {
                Train train = new Train(res.getInt("train_id"));
                trains.add(train);
                logger.debug("Added train: {}", train);
            }
        } catch (SQLException e) {
            logger.error("Error processing train data: {}", e.getMessage(), e);
            throw e;
        } finally {
            try {
                if (res != null) res.close();
                stmt.close();
            } catch (SQLException e) {
                logger.error("Error closing resources: {}", e.getMessage(), e);
            }
        }

        return new Response(request.getRequestId(), objectMapper.writeValueAsString(trains));
    }

    private Response InsertSchedule(final Request request, final Connection connection) throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Schedule schedule = objectMapper.readValue(request.getRequestBody(), Schedule.class);

        try {
            connection.setAutoCommit(false);

            final PreparedStatement stmt = connection.prepareStatement(Queries.INSERT_SCHEDULE.query, Statement.RETURN_GENERATED_KEYS);
            stmt.setTime(1, schedule.getTimeArrival());
            stmt.setTime(2, schedule.getTimeDeparture());
            stmt.setString(3, schedule.getStation().getName());
            stmt.setInt(4, schedule.getTrip().getId());
            stmt.executeUpdate();
            ResultSet res = stmt.getGeneratedKeys();
            if (res.next()) {
                schedule.setId(res.getInt(1));
            }

            connection.commit();

            String responseJson = objectMapper.writeValueAsString(schedule);
            logger.debug("Insert schedule response: {}", responseJson);
            return new Response(request.getRequestId(), responseJson);
        } catch (SQLException e) {
            connection.rollback();
            logger.error("SQL error inserting schedule: {}", e.getMessage());
            throw new SQLException("Erreur lors de l'insertion de l'horaire: " + e.getMessage(), e);
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private Response SelectAllSchedules(final Request request, final Connection connection) throws SQLException, JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Statement stmt = connection.createStatement();
        final ResultSet res = stmt.executeQuery(Queries.SELECT_ALL_SCHEDULES.query);

        Schedules schedules = new Schedules();
        while (res.next()) {
            schedules.add(
                    new Schedule(
                            res.getInt("schedule_id"),
                            res.getTime("schedule_time_arrival"),
                            res.getTime("schedule_time_departure"),
                            new Station(res.getString("station_name")),
                            new Trip(res.getInt("trip_id"), new Train(res.getInt("train_id")))
                    )
            );
        }
        return new Response(request.getRequestId(), objectMapper.writeValueAsString(schedules));
    }

    private Response UpdateSchedule(final Request request, final Connection connection) throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Schedule schedule = objectMapper.readValue(request.getRequestBody(), Schedule.class);

        try {
            PreparedStatement updateStmt = connection.prepareStatement(Queries.UPDATE_SCHEDULE.query);
            updateStmt.setTime(1, schedule.getTimeArrival());
            updateStmt.setTime(2, schedule.getTimeDeparture());
            updateStmt.setString(3, schedule.getStation().getName());
            updateStmt.setInt(4, schedule.getId());
            int rowsAffected = updateStmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Aucun horaire n'a été mis à jour. L'horaire " + schedule.getId() + " n'existe pas.");
            }

            return new Response(request.getRequestId(), objectMapper.writeValueAsString(schedule));
        } catch (SQLException e) {
            logger.error("SQL error updating schedule: {}", e.getMessage());
            throw new SQLException("Erreur lors de la mise à jour de l'horaire: " + e.getMessage(), e);
        }
    }

    private Response DeleteSchedule(final Request request, final Connection connection) throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Schedule schedule = objectMapper.readValue(request.getRequestBody(), Schedule.class);

        try {
            PreparedStatement deleteStmt = connection.prepareStatement(Queries.DELETE_SCHEDULE.query);
            deleteStmt.setInt(1, schedule.getId());
            int rowsAffected = deleteStmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Aucun horaire n'a été supprimé. L'horaire " + schedule.getId() + " n'existe pas.");
            }

            return new Response(request.getRequestId(), objectMapper.writeValueAsString(schedule));
        } catch (SQLException e) {
            logger.error("SQL error deleting schedule: {}", e.getMessage());
            throw new SQLException("Erreur lors de la suppression de l'horaire: " + e.getMessage(), e);
        }
    }

    private Response InsertAlert(final Request request, final Connection connection) throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Alert alert = objectMapper.readValue(request.getRequestBody(), Alert.class);
        final PreparedStatement stmt = connection.prepareStatement(Queries.INSERT_ALERT.query);
        stmt.setString(1, alert.getMessage());
        stmt.setTime(2, alert.getTime());
        stmt.setInt(3, alert.getDuration());
        stmt.setString(4, alert.getGravity().getType());
        stmt.setInt(5, alert.getTrain().getId());
        stmt.executeUpdate();
        ResultSet res = stmt.getGeneratedKeys();
        if (res.next()) alert.setId(res.getInt(1));

        return new Response(request.getRequestId(), objectMapper.writeValueAsString(alert));
    }

    private Response DeleteAlert(final Request request, final Connection connection) throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Alert alert = objectMapper.readValue(request.getRequestBody(), Alert.class);
        final PreparedStatement stmt = connection.prepareStatement(Queries.DELETE_ALERT.query);
        stmt.setInt(1, alert.getId());
        stmt.executeUpdate();

        return new Response(request.getRequestId(), objectMapper.writeValueAsString(alert));
    }

    private Response DeleteTrain(final Request request, final Connection connection) throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Train train = objectMapper.readValue(request.getRequestBody(), Train.class);

        try {
            PreparedStatement deleteStmt = connection.prepareStatement(Queries.DELETE_TRAIN.query);
            deleteStmt.setInt(1, train.getId());
            int rowsAffected = deleteStmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Aucun train n'a été supprimé. Le train " + train.getId() + " n'existe pas.");
            }
        } catch (SQLException e) {
            logger.error("SQL error deleting train: {}", e.getMessage());
            throw new SQLException("Erreur lors de la suppression du train: " + e.getMessage(), e);
        }
        return new Response(request.getRequestId(), objectMapper.writeValueAsString(train));
    }

    private Response SelectAllAlerts(final Request request, final Connection connection)
            throws SQLException, JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Alerts alerts = new Alerts();
        final Statement stmt = connection.createStatement();
        final ResultSet res = stmt.executeQuery(Queries.SELECT_ALL_ALERTS.query);

        while (res.next()) {
            alerts.add(
                    new Alert(
                            res.getInt("alert_id"),
                            res.getString("alert_message"),
                            res.getTime("alert_time"),
                            res.getInt("alert_duration"),
                            AlertGravity.getByTypeName(res.getString("alert_gravity_type")),
                            new Train(res.getInt("train_id"))
                    )
            );
        }
        return new Response(request.getRequestId(), objectMapper.writeValueAsString(alerts));
    }

    private Response SelectAllStations(final Request request, final Connection connection)
            throws SQLException, JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Statement stmt = connection.createStatement();
        final ResultSet res = stmt.executeQuery(Queries.SELECT_ALL_STATIONS.query);
        
        Stations stations = new Stations();
        try {
            while (res.next()) {
                Station station = new Station(res.getString("station_name"));
                stations.add(station);
                logger.debug("Added station: {}", station);
            }
        } catch (SQLException e) {
            logger.error("Error processing station data: {}", e.getMessage(), e);
            throw e;
        } finally {
            try {
                if (res != null) res.close();
                stmt.close();
            } catch (SQLException e) {
                logger.error("Error closing resources: {}", e.getMessage(), e);
            }
        }
        
        return new Response(request.getRequestId(), objectMapper.writeValueAsString(stations));
    }
}
