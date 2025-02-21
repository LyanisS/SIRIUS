package edu.ezip.ing1.pds.business.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "track")
public enum Track {
    UNKNOWN(0, "Voie inconnue"),
    A(1, "Voie A"),
    B(2, "Voie B");

    @JsonProperty("id")
    private final int id;

    @JsonProperty("name")
    private final String name;

    Track(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public static Track getById(int id) {
        for (Track t : values()) {
            if (t.getId() == id)
                return t;
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return "Track{" +
                "id=" + this.id +
                ", name=" + this.name +
                '}';
    }
}
