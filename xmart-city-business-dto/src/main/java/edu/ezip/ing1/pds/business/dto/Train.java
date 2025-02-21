package edu.ezip.ing1.pds.business.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "train")
public class Train {
    @JsonProperty("id")
    private int id;

    @JsonProperty("status")
    private TrainStatus status;

    @JsonProperty("track_element")
    private TrackElement trackElement;

    public Train() {}

    public Train(int id, TrainStatus status, TrackElement trackElement) {
        this.id = id;
        this.status = status;
        this.trackElement = trackElement;
    }

    public int getId() {
        return this.id;
    }

    public TrainStatus getStatus() {
        return this.status;
    }

    public TrackElement getTrackElement() {
        return this.trackElement;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(TrainStatus status) {
        this.status = status;
    }

    public void setTrackElement(TrackElement trackElement) {
        this.trackElement = trackElement;
    }

    @Override
    public String toString() {
        return "Train{" +
                "id=" + this.id +
                ", status=" + this.status +
                ", trackElement=" + this.trackElement +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        Train t = (Train) o;
        return this.id == t.id;
    }
}
