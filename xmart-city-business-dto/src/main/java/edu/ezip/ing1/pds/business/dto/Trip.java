package edu.ezip.ing1.pds.business.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "trip")
public class Trip {
    @JsonProperty("id")
    private int id;
    @JsonProperty("train")
    private Train train;
    @JsonProperty("driver")
    private Person driver;

    public Trip() {}

    public Trip(int id, Train train, Person driver) {
        this.id = id;
        this.train = train;
        this.driver = driver;
    }

    public Trip(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Train getTrain() {
        return this.train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    public Person getDriver() {
        return this.driver;
    }

    public void setDriver(Person driver) {
        this.driver = driver;
    }

    @Override
    public String toString() {
        return "Trip{" +
                "id=" + this.id +
                ", train=" + this.train +
                ", driver=" + this.driver +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        Trip t = (Trip) o;
        return this.id == t.id;
    }
}
