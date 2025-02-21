package edu.ezip.ing1.pds.business.dto;

import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Stations {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("stations")
    private Set<Station> stations = new LinkedHashSet<Station>();

    public Set<Station> getStations() {
        return this.stations;
    }

    public void setStations(Set<Station> station) {
        this.stations = station;
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
