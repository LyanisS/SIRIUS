package edu.ezip.ing1.pds.business.dto;

import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Trains {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("trains")
    private Set<Train> trains = new LinkedHashSet<Train>();

    public Set<Train> getTrains() {
        return this.trains;
    }

    public void setTrains(Set<Train> trains) {
        this.trains = trains;
    }

    public final Trains add(final Train train) {
        this.trains.add(train);
        return this;
    }

    @Override
    public String toString() {
        return "Trains{" +
                "trains=" + this.trains +
                '}';
    }
}
