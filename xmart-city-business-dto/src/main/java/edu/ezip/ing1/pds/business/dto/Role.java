package edu.ezip.ing1.pds.business.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "role")
public enum Role {
    UNKNOWN(0, "inconnu"),
    CREG(1, "CREG"),
    GT(2, "GT"),
    DRIVER(2, "Conducteur");

    @JsonProperty("id")
    private final int id;

    @JsonProperty("name")
    private final String name;

    Role(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public static Role getById(int id) {
        for (Role r : values()) {
            if (r.getId() == id)
                return r;
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + this.id +
                ", name=" + this.name +
                '}';
    }
}
