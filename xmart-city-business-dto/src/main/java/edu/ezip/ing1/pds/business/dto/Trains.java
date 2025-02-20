package edu.ezip.ing1.pds.business.dto;

import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

public class Trains {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonValue  // This annotation tells Jackson to treat the trains field as the value itself
    private Set<Train> trains = new LinkedHashSet<>();

    public Set<Train> getTrains() {
        return trains;
    }

    public void setTrains(Set<Train> trains) {
        this.trains = trains;
    }

    public final Trains add(final Train train) {
        trains.add(train);
        return this;
    }

    @Override
    public String toString() {
        return "Trains{"
                + "trains=" + trains
                + '}';
    }
}
