package edu.ezip.ing1.pds.business.dto;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "schedule")
public class Schedule {

    @JsonProperty("schedule_id")
    private int scheduleId;

    @JsonProperty("trip_id")
    private int tripId;

    @JsonProperty("track_element_id")
    private int trackElementId;

    @JsonProperty("schedule_stop")
    private boolean scheduleStop;

    @JsonProperty("schedule_datetime")
    private LocalDateTime scheduleDatetime;

    public Schedule() {
    }

    public final Schedule build(final ResultSet resultSet)
            throws SQLException, NoSuchFieldException, IllegalAccessException {
        this.scheduleId = resultSet.getInt("schedule_id");
        this.tripId = resultSet.getInt("trip_id");
        this.trackElementId = resultSet.getInt("track_element_id");
        this.scheduleStop = resultSet.getBoolean("schedule_stop");

// Convertir le Timestamp SQL en LocalDateTime
        Timestamp timestamp = resultSet.getTimestamp("schedule_datetime");
        this.scheduleDatetime = (timestamp != null) ? timestamp.toLocalDateTime() : null;

        return this;
    }

    /*  public final Schedule build(final ResultSet resultSet)
    throws SQLException, NoSuchFieldException, IllegalAccessException {
        setFieldsFromResultset(resultSet,
                "schedule_id",
                "trip_id",
                "track_element_id",
                "schedule_stop",
                "schedule_datetime"
        );
        return this;
    }*/
    public final PreparedStatement build(PreparedStatement preparedStatement)
            throws SQLException, NoSuchFieldException, IllegalAccessException {
        return buildPreparedStatement(preparedStatement,
                scheduleId,
                tripId,
                trackElementId,
                // scheduleDatetime,
                (scheduleDatetime != null) ? Timestamp.valueOf(scheduleDatetime) : null,
                scheduleStop
        );
    }

    public Schedule(int scheduleId, int tripId, int trackElementId, LocalDateTime scheduleDatetime, boolean scheduleStop) {
        this.scheduleId = scheduleId;
        this.tripId = tripId;
        this.trackElementId = trackElementId;
        this.scheduleDatetime = scheduleDatetime;
        this.scheduleStop = scheduleStop;
    }

    @JsonProperty("schedule_id")
    public int getScheduleId() {
        return scheduleId;
    }

    @JsonProperty("trip_id")
    public int getTripId() {
        return tripId;
    }

    @JsonProperty("track_element_id")
    public int getTrackElementId() {
        return trackElementId;
    }

    @JsonProperty("schedule_stop")
    public boolean getScheduleStop() {
        return scheduleStop;
    }

    @JsonProperty("schedule_datetime")
    public LocalDateTime getScheduleDatetime() {
        return scheduleDatetime;
    }

    @JsonProperty("schedule_id")
    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    @JsonProperty("trip_id")
    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    @JsonProperty("track_element_id")
    public void setTrackElementId(int trackElementId) {
        this.trackElementId = trackElementId;
    }

    @JsonProperty("schedule_stop")
    public void setScheduleStop(boolean scheduleStop) {
        this.scheduleStop = scheduleStop;
    }

    @JsonProperty("schedule_datetime")
    public void setScheduleDatetime(LocalDateTime scheduleDatetime) {
        this.scheduleDatetime = scheduleDatetime;
    }

    private void setFieldsFromResultset(final ResultSet resultSet, final String... fieldNames)
            throws NoSuchFieldException, SQLException, IllegalAccessException {
        for (final String fieldName : fieldNames) {
            final Field field = this.getClass().getDeclaredField(mapColumnToField(fieldName));
            field.set(this, resultSet.getObject(fieldName));
        }
    }

    private String mapColumnToField(String columnName) {
        return columnName.replace("schedule_", "")
                .replace("trip_", "")
                .replace("track_element_", "")
                .replace("_", "");
    }

    private final PreparedStatement buildPreparedStatement(PreparedStatement preparedStatement, final Object... values)
            throws SQLException {
        for (int i = 0; i < values.length; i++) {
            preparedStatement.setObject(i + 1, values[i]);
        }
        return preparedStatement;
    }

    @Override
    public String toString() {
        return "Schedule{"
                + "scheduleId=" + scheduleId
                + ", tripId=" + tripId
                + ", trackElementId=" + trackElementId
                + ", scheduleDatetime=" + scheduleDatetime
                + ", scheduleStop=" + scheduleStop
                + '}';
    }
}
