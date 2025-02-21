package edu.ezip.ing1.pds.business.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.sql.Timestamp;

@JsonRootName(value = "alert")
public class Alert {
    @JsonProperty("id")
    private int id;
    @JsonProperty("message")
    private String message;
    @JsonProperty("timestamp")
    private Timestamp timestamp;
    @JsonProperty("gravity")
    private AlertGravity gravity;
    @JsonProperty("train")
    private Train train;

    public Alert() {}

    public Alert(int id, String message, Timestamp timestamp, AlertGravity gravity, Train train) {
        this.id = id;
        this.message = message;
        this.timestamp = timestamp;
        this.gravity = gravity;
        this.train = train;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public AlertGravity getGravity() {
        return this.gravity;
    }

    public void setGravity(AlertGravity gravity) {
        this.gravity = gravity;
    }

    public Train getTrain() {
        return this.train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    @Override
    public String toString() {
        return "Alert{" +
                "id=" + this.id +
                ", message=" + this.message +
                ", timestamp=" + this.timestamp +
                ", gravity=" + this.gravity +
                ", train=" + this.train +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        Alert a = (Alert) o;
        return this.id == a.id;
    }
}
