package edu.ezip.ing1.pds.business.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "track_element_type")
public enum TrackElementType {
    UNKNOWN(0, "inconnu"),
    CDV(1, "CDV"),
    SWITCH(2, "Aiguille");

    @JsonProperty("id")
    private final int id;

    @JsonProperty("name")
    private final String name;

    TrackElementType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public static TrackElementType getById(int id) {
        for (TrackElementType tep : values()) {
            if (tep.getId() == id)
                return tep;
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return "TrackElementType{" +
                "id=" + this.id +
                ", name=" + this.name +
                '}';
    }
}
