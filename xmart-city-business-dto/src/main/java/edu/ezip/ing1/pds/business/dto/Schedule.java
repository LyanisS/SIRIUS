package edu.ezip.ing1.pds.business.dto;

import java.sql.Time;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "schedule")
public class Schedule {
    @JsonProperty("id")
    private int id;

    @JsonProperty("time_arrival")
    private Time timeArrival;

    @JsonProperty("time_departure")
    private Time timeDeparture;

    @JsonProperty("station")
    private Station station;

    @JsonProperty("trip")
    private Trip trip;

    public Schedule() {}

    public Schedule(int id, Time timeArrival, Time timeDeparture, Station station, Trip trip) {
        this.id = id;
        this.timeArrival = timeArrival;
        this.timeDeparture = timeDeparture;
        this.station = station;
        this.trip = trip;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Trip getTrip() {
        return this.trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public Station getStation() {
        return this.station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public Time getTimeArrival() {
        return this.timeArrival;
    }

    public void setTimeArrival(Time timeArrival) {
        this.timeArrival = timeArrival;
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "id=" + this.id +
                ", trip=" + this.trip +
                ", station=" + this.station +
                ", timeArrival=" + this.timeArrival +
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
