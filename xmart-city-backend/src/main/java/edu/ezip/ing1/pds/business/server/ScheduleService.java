package edu.ezip.ing1.pds.business.server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.Schedule;
import edu.ezip.ing1.pds.commons.Request;
import edu.ezip.ing1.pds.commons.Response;

public class ScheduleService {

    private final static String LoggingLabel = "B u s i n e s s - S c h e d u l e - S e r v e r";
    private final Logger logger = LoggerFactory.getLogger(LoggingLabel);

    private enum Queries {
        SELECT_ALL_SCHEDULES("SELECT schedule_id, trip_id, track_element_id, schedule_stop, schedule_datetime FROM schedule"),
        INSERT_SCHEDULE("INSERT INTO schedule (schedule_id, trip_id, track_element_id, schedule_stop, schedule_datetime) VALUES (?, ?, ?, ?, ?)");

        private final String query;

        private Queries(final String query) {
            this.query = query;
        }
    }

    public static ScheduleService inst = null;

    public static final ScheduleService getInstance() {
        if (inst == null) {
            inst = new ScheduleService();
        }
        return inst;
    }

    private ScheduleService() {
    }

    public final Response dispatch(final Request request, final Connection connection)
            throws SQLException, IOException {
        Response response = null;

        final Queries queryEnum = Enum.valueOf(Queries.class, request.getRequestOrder());
        switch (queryEnum) {
            case SELECT_ALL_SCHEDULES:
                response = selectAllSchedules(request, connection);
                break;
            case INSERT_SCHEDULE:
                response = insertSchedule(request, connection);
                break;
            default:
                break;
        }
        return response;
    }

    private Response insertSchedule(final Request request, final Connection connection) throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Schedule schedule = objectMapper.readValue(request.getRequestBody(), Schedule.class);
        final PreparedStatement stmt = connection.prepareStatement(Queries.INSERT_SCHEDULE.query, Statement.RETURN_GENERATED_KEYS);

        stmt.setInt(1, schedule.getScheduleId());
        stmt.setInt(2, schedule.getTripId());
        stmt.setInt(3, schedule.getTrackElementId());
        stmt.setBoolean(4, schedule.getScheduleStop());
        // stmt.setDate(5, schedule.getScheduleDatetime());
        stmt.setTimestamp(5, Timestamp.valueOf(schedule.getScheduleDatetime()));

        stmt.executeUpdate();

        final ResultSet res = stmt.getGeneratedKeys();
        if (res.next()) {
            schedule.setScheduleId(res.getInt(1));
        }

        return new Response(request.getRequestId(), objectMapper.writeValueAsString(schedule));
    }

    // Cella il faut la revoir
    public void addSchedule(Schedule schedule, Connection connection) throws SQLException {
        final PreparedStatement stmt = connection.prepareStatement(Queries.INSERT_SCHEDULE.query);
        stmt.setInt(1, schedule.getScheduleId());
        stmt.setInt(2, schedule.getTripId());
        stmt.setInt(3, schedule.getTrackElementId());
        stmt.setBoolean(4, schedule.getScheduleStop());
        stmt.setTimestamp(5, Timestamp.valueOf(schedule.getScheduleDatetime()));

        stmt.executeUpdate();
    }

    private Response selectAllSchedules(final Request request, final Connection connection) throws SQLException, JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Statement stmt = connection.createStatement();
        final ResultSet res = stmt.executeQuery(Queries.SELECT_ALL_SCHEDULES.query);

        StringBuilder schedulesJson = new StringBuilder("[");
        boolean first = true;
        while (res.next()) {
            Schedule schedule = new Schedule();
            schedule.setScheduleId(res.getInt("schedule_id"));
            schedule.setTripId(res.getInt("trip_id"));
            schedule.setTrackElementId(res.getInt("track_element_id"));
            schedule.setScheduleStop(res.getBoolean("schedule_stop"));
            // schedule.setScheduleDatetime(res.getdatetime("schedule_datetime"));
            schedule.setScheduleDatetime(res.getTimestamp("schedule_datetime").toLocalDateTime());

            if (!first) {
                schedulesJson.append(",");
            }
            schedulesJson.append(objectMapper.writeValueAsString(schedule));
            first = false;
        }
        schedulesJson.append("]");

        return new Response(request.getRequestId(), schedulesJson.toString());
    }

}
