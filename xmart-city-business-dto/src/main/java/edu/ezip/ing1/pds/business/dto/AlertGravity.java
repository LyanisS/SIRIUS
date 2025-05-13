package edu.ezip.ing1.pds.business.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.awt.*;

@JsonRootName(value = "alert_gravity")
public enum AlertGravity {
    UNKNOWN("Inconnu"),
    INFO( "Info"),
    WARNING( "Avertissement", new Color(243, 156, 18)),
    HIGH( "Critique", new Color(231, 76, 60));

    @JsonProperty("type")
    private final String type;
    @JsonProperty("color")
    private final Color color;

    AlertGravity(String type, Color color) {
        this.type = type;
        this.color = color;
    }

    AlertGravity(String type) {
        this.type = type;
        this.color = new Color(52, 152, 219);
    }

    public String getType() {
        return this.type;
    }

    public Color getColor() {
        return this.color;
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
