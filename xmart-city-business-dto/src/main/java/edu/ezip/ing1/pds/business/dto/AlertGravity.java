package edu.ezip.ing1.pds.business.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "alert_gravity")
public enum AlertGravity {
    UNKNOWN("inconnu"),
    LOW( "Faible"),
    MEDIUM( "Moyen"),
    HIGH( "Élevé");

    @JsonProperty("type")
    private final String type;

    AlertGravity(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public static AlertGravity getByTypeName(String type) {
        for (AlertGravity ag : values()) {
            if (ag.getType().equals(type))
                return ag;
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return "AlertGravity{type=" + this.type + '}';
    }
}
