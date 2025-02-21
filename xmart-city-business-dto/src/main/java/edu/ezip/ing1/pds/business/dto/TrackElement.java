package edu.ezip.ing1.pds.business.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "trackElement")
public class TrackElement {
    @JsonProperty("id")
    private int id;

    @JsonProperty("isWorking")
    private boolean isWorking;

    @JsonProperty("switchPosition")
    private SwitchPosition switchPosition;

    @JsonProperty("type")
    private TrackElementType type;

    @JsonProperty("track")
    private Track track;

    @JsonProperty("station")
    private Station station;

    public TrackElement() {}

    public TrackElement(int id, boolean isWorking, SwitchPosition switchPosition, TrackElementType type, Track track, Station station) {
        this.id = id;
        this.isWorking = isWorking;
        this.switchPosition = switchPosition;
        this.type = type;
        this.track = track;
        this.station = station;
    }

    public TrackElement(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public boolean isWorking() {
        return this.isWorking;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setWorking(boolean isWorking) {
        this.isWorking = isWorking;
    }

    public SwitchPosition getSwitchPosition() {
        return this.switchPosition;
    }

    public void setSwitchPosition(SwitchPosition switchPosition) {
        this.switchPosition = switchPosition;
    }

    public TrackElementType getType() {
        return this.type;
    }

    public void setType(TrackElementType type) {
        this.type = type;
    }

    public Track getTrack() {
        return this.track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }

    public Station getStation() {
        return this.station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    @Override
    public String toString() {
        return "TrackElement{" +
                "id=" + this.id +
                ", isWorking=" + this.isWorking +
                ", switchPosition=" + this.switchPosition +
                ", type=" + this.type +
                ", track=" + this.track +
                ", station=" + this.station +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        TrackElement te = (TrackElement) o;
        return this.id == te.id;
    }
}
