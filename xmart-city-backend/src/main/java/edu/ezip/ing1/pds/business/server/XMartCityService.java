package edu.ezip.ing1.pds.business.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ezip.ing1.pds.business.dto.*;
import edu.ezip.ing1.pds.commons.Request;
import edu.ezip.ing1.pds.commons.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;

public class XMartCityService {

    private final static String LoggingLabel = "B u s i n e s s - S e r v e r";
    private final Logger logger = LoggerFactory.getLogger(LoggingLabel);

    private enum Queries {
        SELECT_ALL_TRAINS("SELECT train_id, train_status_id, track_element_id, track_element_is_working, switch_position_id, track_element_type_id, track_id, station_id, station_name FROM train JOIN track_element USING (track_element_id) JOIN station USING (station_id);"),
        INSERT_TRAIN("INSERT INTO trains (train_id, train_status_id, track_element_id) VALUES (?, ?, ?);"),
        SELECT_ALL_SCHEDULES("SELECT schedule_id, schedule_timestamp, schedule_stop, schedule.track_element_id, track_element_is_working, switch_position_id, track_element_type_id, track_id, station_id, station_name, trip_id, train_id, train_status_id, person_id, person_first_name, person_last_name, person_login FROM schedule JOIN track_element ON schedule.track_element_id=track_element.track_element_id JOIN station USING (station_id) JOIN trip USING (trip_id) JOIN train USING (train_id) JOIN person USING (person_id);"),
        INSERT_SCHEDULE("INSERT INTO schedule (schedule_timestamp, schedule_stop, track_element_id, trip_id) VALUES (?, ?, ?, ?);"),
        SELECT_ALL_ALERTS("SELECT alert_id, alert_message, alert_timestamp, alert_gravity_id, train_id, train_status_id FROM alert JOIN train USING (train_id);"),
        INSERT_ALERT("INSERT INTO alert (alert_message, alert_timestamp, alert_gravity_id, train_id) VALUES (?, ?, ?, ?);");

        private final String query;

        private Queries(final String query) {
            this.query = query;
        }
    }

    public static XMartCityService inst = null;
    public static final XMartCityService getInstance()  {
        if(inst == null) {
            inst = new XMartCityService();
        }
        return inst;
    }

    private XMartCityService() {}

    public final Response dispatch(final Request request, final Connection connection)
            throws InvocationTargetException, IllegalAccessException, SQLException, IOException {
        Response response = null;

        final Queries queryEnum = Enum.valueOf(Queries.class, request.getRequestOrder());
        switch(queryEnum) {
            case SELECT_ALL_TRAINS:
                response = SelectAllTrains(request, connection);
                break;
            case INSERT_TRAIN:
                response = InsertTrain(request, connection);
                break;
            case SELECT_ALL_SCHEDULES:
                response = SelectAllSchedules(request, connection);
                break;
            case INSERT_SCHEDULE:
                response = InsertSchedule(request, connection);
                break;
            case SELECT_ALL_ALERTS:
                response = SelectAllAlerts(request, connection);
                break;
            case INSERT_ALERT:
                response = InsertAlert(request, connection);
                break;
            default:
                break;
        }

        return response;
    }

    private Response InsertTrain(final Request request, final Connection connection) throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Train train = objectMapper.readValue(request.getRequestBody(), Train.class);
        final PreparedStatement stmt = connection.prepareStatement(Queries.INSERT_TRAIN.query);
        stmt.setInt(1, train.getStatus().getId());
        stmt.setInt(2, train.getTrackElement().getId());
        stmt.executeUpdate();
        ResultSet res = stmt.getGeneratedKeys();
        if (res.next()) train.setId(res.getInt(1));

        return new Response(request.getRequestId(), objectMapper.writeValueAsString(train));
    }

    private Response SelectAllTrains(final Request request, final Connection connection) throws SQLException, JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Statement stmt = connection.createStatement();
        final ResultSet res = stmt.executeQuery(Queries.SELECT_ALL_TRAINS.query);
        Trains trains = new Trains();
        while (res.next()) {
            trains.add(
                new Train(
                    res.getInt("train_id"),
                    TrainStatus.getById(res.getInt("train_status_id")),
                    new TrackElement(
                        res.getInt("track_element_id"),
                        res.getBoolean("track_element_is_working"),
                        SwitchPosition.getById(res.getInt("switch_position_id")),
                        TrackElementType.getById(res.getInt("track_element_type_id")),
                        Track.getById(res.getInt("track_id")),
                        new Station(
                            res.getInt("station_id"),
                            res.getString("station_name")
                        )
                    )
                )
            );
        }
        return new Response(request.getRequestId(), objectMapper.writeValueAsString(trains));
    }

    private Response InsertSchedule(final Request request, final Connection connection) throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Schedule schedule = objectMapper.readValue(request.getRequestBody(), Schedule.class);
        final PreparedStatement stmt = connection.prepareStatement(Queries.INSERT_SCHEDULE.query, Statement.RETURN_GENERATED_KEYS);
        stmt.setTimestamp(1, schedule.getTimestamp());
        stmt.setBoolean(2, schedule.getStop());
        stmt.setInt(3, schedule.getTrackElement().getId());
        stmt.setInt(4, schedule.getTrip().getId());
        stmt.executeUpdate();
        ResultSet res = stmt.getGeneratedKeys();
        if (res.next()) schedule.setId(res.getInt(1));

        return new Response(request.getRequestId(), objectMapper.writeValueAsString(schedule));
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
                    res.getTimestamp("schedule_timestamp"),
                    res.getBoolean("schedule_stop"),
                    new TrackElement(
                        res.getInt("track_element_id"),
                        res.getBoolean("track_element_is_working"),
                        SwitchPosition.getById(res.getInt("switch_position_id")),
                        TrackElementType.getById(res.getInt("track_element_type_id")),
                        Track.getById(res.getInt("track_id")),
                        new Station(
                            res.getInt("station_id"),
                            res.getString("station_name")
                        )
                    ),
                    new Trip(
                        res.getInt("trip_id"),
                        new Train(
                            res.getInt("train_id"),
                            TrainStatus.getById(res.getInt("train_status_id")),
                            null
                        ),
                        new Person(
                            res.getInt("person_id"),
                            res.getString("person_last_name"),
                            res.getString("person_first_name"),
                            res.getString("person_login"),
                            null
                        )
                    )
                )
            );
        }
        return new Response(request.getRequestId(), objectMapper.writeValueAsString(schedules));
    }

    private Response InsertAlert(final Request request, final Connection connection) throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Alert alert = objectMapper.readValue(request.getRequestBody(), Alert.class);
        final PreparedStatement stmt = connection.prepareStatement(Queries.INSERT_ALERT.query);
        stmt.setString(1, alert.getMessage());
        stmt.setTimestamp(2, alert.getTimestamp());
        stmt.setInt(3, alert.getGravity().getId());
        stmt.setInt(4, alert.getTrain().getId());
        stmt.executeUpdate();
        ResultSet res = stmt.getGeneratedKeys();
        if (res.next()) alert.setId(res.getInt(1));

        return new Response(request.getRequestId(), objectMapper.writeValueAsString(alert));
    }

    private Response SelectAllAlerts(final Request request, final Connection connection) throws SQLException, JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Statement stmt = connection.createStatement();
        final ResultSet res = stmt.executeQuery(Queries.SELECT_ALL_ALERTS.query);
        Alerts alerts = new Alerts();
        while (res.next()) {
            alerts.add(
                new Alert(
                    res.getInt("alert_id"),
                    res.getString("alert_message"),
                    res.getTimestamp("alert_timestamp"),
                    AlertGravity.getById(res.getInt("alert_gravity_id")),
                    new Train(res.getInt("train_id"), TrainStatus.getById(res.getInt("train_status_id")), null)
                )
            );
        }
        return new Response(request.getRequestId(), objectMapper.writeValueAsString(alerts));
    }
}
