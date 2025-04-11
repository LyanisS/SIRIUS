package edu.ezip.ing1.pds.business.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "role")
public enum Role {
    UNKNOWN("inconnu"),
    CREG("CREG"),
    GT("GT"),
    DRIVER("Conducteur");

    @JsonProperty("name")
    private final String name;

    Role(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static Role getByName(String name) {
        for (Role r : values()) {
            if (r.getName().equals(name))
                return r;
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return "Role{name=" + this.name + '}';
    }
}
