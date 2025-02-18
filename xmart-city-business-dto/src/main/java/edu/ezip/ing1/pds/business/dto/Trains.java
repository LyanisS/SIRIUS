package edu.ezip.ing1.pds.business.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.LinkedHashSet;
import java.util.Set;

public class Trains {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("trains")
    private Set<Train> trains = new LinkedHashSet<Train>();

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
        return "Trains{" +
                "trains=" + trains +
                '}';
    }
}