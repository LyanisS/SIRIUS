package edu.ezip.ing1.pds.business.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;



@JsonRootName(value = "train")
public class Train {
    private int trainId;
    private int trainStatusId;
    private int trackElementId;
    private String currentStation;
    private String direction;
    private String arrivalTime;

    public Train() {
    }

    public final Train build(final ResultSet resultSet)
            throws SQLException, NoSuchFieldException, IllegalAccessException {
        setFieldsFromResultset(resultSet, 
            "train_id", 
            "train_status_id",
            "track_element_id",
            "station_name",
            "direction",
            "schedule_datetime"
        );
        return this;
    }

    public final PreparedStatement build(PreparedStatement preparedStatement)
            throws SQLException, NoSuchFieldException, IllegalAccessException {
        return buildPreparedStatement(preparedStatement, 
            trainId, 
            trainStatusId,
            trackElementId
        );
    }

    public Train(int trainId, int trainStatusId, int trackElementId) {
        this.trainId = trainId;
        this.trainStatusId = trainStatusId;
        this.trackElementId = trackElementId;
    }


    @JsonProperty("train_id")
    public int getTrainId() { return trainId; }

    @JsonProperty("train_status_id")
    public int getTrainStatusId() { return trainStatusId; }

    @JsonProperty("track_element_id")
    public int getTrackElementId() { return trackElementId; }

    @JsonProperty("current_station")
    public String getCurrentStation() { return currentStation; }

    @JsonProperty("direction")
    public String getDirection() { return direction; }

    @JsonProperty("arrival_time")
    public String getArrivalTime() { return arrivalTime; }

    
    @JsonProperty("train_id")
    public void setTrainId(int trainId) { this.trainId = trainId; }

    @JsonProperty("train_status_id")
    public void setTrainStatusId(int trainStatusId) { this.trainStatusId = trainStatusId; }

    @JsonProperty("track_element_id")
    public void setTrackElementId(int trackElementId) { this.trackElementId = trackElementId; }

    @JsonProperty("current_station")
    public void setCurrentStation(String currentStation) { this.currentStation = currentStation; }

    @JsonProperty("direction")
    public void setDirection(String direction) { this.direction = direction; }

    @JsonProperty("arrival_time")
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }

    private void setFieldsFromResultset(final ResultSet resultSet, final String... fieldNames)
            throws NoSuchFieldException, SQLException, IllegalAccessException {
        for(final String fieldName : fieldNames) {
            final Field field = this.getClass().getDeclaredField(mapColumnToField(fieldName));
            field.set(this, resultSet.getObject(fieldName));
        }
    }

    private String mapColumnToField(String columnName) {
        return columnName.replace("train_", "")
                        .replace("schedule_", "")
                        .replace("station_", "")
                        .replace("datetime", "Time")
                        .replace("_", "");
    }

    private final PreparedStatement buildPreparedStatement(PreparedStatement preparedStatement, final Object... values)
            throws SQLException {
        for(int i = 0; i < values.length; i++) {
            preparedStatement.setObject(i + 1, values[i]);
        }
        return preparedStatement;
    }

    @Override
    public String toString() {
        return "Train{" +
                "trainId=" + trainId +
                ", trainStatusId=" + trainStatusId +
                ", trackElementId=" + trackElementId +
                ", currentStation='" + currentStation + '\'' +
                ", direction='" + direction + '\'' +
                ", arrivalTime='" + arrivalTime + '\'' +
                '}';
    }
}