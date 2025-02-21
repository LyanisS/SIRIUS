package edu.ezip.ing1.pds.business.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "switch_position")
public enum SwitchPosition {
    UNKNOWN(0, "inconnu"),
    LEFT(1, "Gauche"),
    RIGHT(2, "Droite");

    @JsonProperty("id")
    private final int id;

    @JsonProperty("name")
    private final String name;

    SwitchPosition(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public static SwitchPosition getById(int id) {
        for (SwitchPosition sp : values()) {
            if (sp.getId() == id)
                return sp;
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return "SwitchPosition{" +
                "id=" + this.id +
                ", name=" + this.name +
                '}';
    }
}
