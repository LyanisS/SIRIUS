package edu.ezip.ing1.pds.business.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashSet;
import java.util.Set;

public class Alerts {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("alerts")
    private Set<Alert> alerts = new LinkedHashSet<Alert>();

    public Set<Alert> getAlerts() {
        return this.alerts;
    }

    public void setAlerts(Set<Alert> alerts) {
        this.alerts = alerts;
    }

    public final Alerts add(final Alert alert) {
        this.alerts.add(alert);
        return this;
    }

    @Override
    public String toString() {
        return "Alerts{" +
                "alerts=" + this.alerts +
                '}';
    }
}
