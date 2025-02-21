package edu.ezip.ing1.pds.business.dto;

import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Trips {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("trips")
    private Set<Trip> trips = new LinkedHashSet<Trip>();

    public Set<Trip> getTrips() {
        return this.trips;
    }

    public void setTrips(Set<Trip> trips) {
        this.trips = trips;
    }

    public final Trips add(final Trip trip) {
        this.trips.add(trip);
        return this;
    }

    @Override
    public String toString() {
        return "Trips{" +
                "trips=" + this.trips +
                '}';
    }
}
