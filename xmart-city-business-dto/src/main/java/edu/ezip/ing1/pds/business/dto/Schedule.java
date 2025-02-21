package edu.ezip.ing1.pds.business.dto;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "schedule")
public class Schedule {
    @JsonProperty("id")
    private int id;

    @JsonProperty("stop")
    private boolean stop;

    @JsonProperty("timestamp")
    private Timestamp timestamp;

    @JsonProperty("track_element")
    private TrackElement trackElement;

    @JsonProperty("trip")
    private Trip trip;

    public Schedule() {}

    public Schedule(int id, Timestamp timestamp, boolean stop, TrackElement trackElement, Trip trip) {
        this.id = id;
        this.timestamp = timestamp;
        this.stop = stop;
        this.trackElement = trackElement;
        this.trip = trip;
    }

    public int getId() {
        return this.id;
    }

    public Trip getTrip() {
        return this.trip;
    }

    public TrackElement getTrackElement() {
        return this.trackElement;
    }

    public boolean getStop() {
        return this.stop;
    }

    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public void setTrackElement(TrackElement trackElement) {
        this.trackElement = trackElement;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "id=" + this.id +
                ", trip=" + this.trip +
                ", trackElement=" + this.trackElement +
                ", timestamp=" + this.timestamp +
                ", stop=" + this.stop +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        Schedule s = (Schedule) o;
        return this.id == s.id;
    }
}
