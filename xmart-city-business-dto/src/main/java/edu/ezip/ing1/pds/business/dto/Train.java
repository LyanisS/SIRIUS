package edu.ezip.ing1.pds.business.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

@JsonRootName(value = "train")
public class Train {
    private int train_id;
    private int train_status_id;
    private int track_element_id;
    private String station_name;
    private LocalDateTime time_in_station;

    public Train() {
    }

    public int getTrain_id() {
        return train_id;
    }

    public void setTrain_id(int train_id) {
        this.train_id = train_id;
    }

    public int getTrain_status_id() {
        return train_status_id;
    }

    public void setTrain_status_id(int train_status_id) {
        this.train_status_id = train_status_id;
    }

    public int getTrack_element_id() {
        return track_element_id;
    }

    public void setTrack_element_id(int track_element_id) {
        this.track_element_id = track_element_id;
    }

    public String getStation_name() {
        return station_name;
    }

    public void setStation_name(String station_name) {
        this.station_name = station_name;
    }

    public LocalDateTime getTime_in_station() {
        return time_in_station;
    }

    public void setTime_in_station(LocalDateTime time_in_station) {
        this.time_in_station = time_in_station;
    }

}
