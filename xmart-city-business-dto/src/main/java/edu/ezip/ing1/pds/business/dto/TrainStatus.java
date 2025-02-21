package edu.ezip.ing1.pds.business.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "train_status")
public enum TrainStatus {
    UNKNOWN(0, "inconnu"),
    EN_CIRCULATION(1, "en circulation"),
    GARE(2, "garé"),
    EN_MAINTENANCE(3, "en maintenance");

    @JsonProperty("id")
    private final int id;
    @JsonProperty("name")
    private final String name;

    TrainStatus(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public static TrainStatus getById(int id) {
        for (TrainStatus ts : values()) {
            if (ts.getId() == id)
                return ts;
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return "TrainStatus{" +
                "id=" + this.id +
                ", name=" + this.name +
                '}';
    }
}
