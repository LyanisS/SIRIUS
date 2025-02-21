package edu.ezip.ing1.pds.business.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "alert_gravity")
public enum AlertGravity {
    UNKNOWN(0, 0, "inconnu"),
    LOW(1, 1, "Faible"),
    MEDIUM(2, 2, "Moyen"),
    HIGH(2, 3, "Élevé");

    @JsonProperty("id")
    private final int id;

    @JsonProperty("level")
    private final int level;

    @JsonProperty("type")
    private final String type;

    AlertGravity(int id, int level, String type) {
        this.id = id;
        this.level = level;
        this.type = type;
    }

    public int getId() {
        return this.id;
    }

    public int getLevel() {
        return this.level;
    }

    public String getType() {
        return this.type;
    }

    public static AlertGravity getById(int id) {
        for (AlertGravity ag : values()) {
            if (ag.getId() == id)
                return ag;
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return "AlertGravity{" +
                "id=" + this.id +
                "level=" + this.level +
                ", type=" + this.type +
                '}';
    }
}
