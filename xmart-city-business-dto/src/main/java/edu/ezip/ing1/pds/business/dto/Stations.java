package edu.ezip.ing1.pds.business.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Stations {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("stations")
    private List<Station> stations = new ArrayList<Station>();

    public List<Station> getStations() {
        return this.stations;
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
    }

    public final Stations add(final Station station) {
        this.stations.add(station);
        return this;
    }

    @Override
    public String toString() {
        return "Stations{" +
                "stations=" + this.stations +
                '}';
    }
}
