package edu.ezip.ing1.pds.business.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashSet;
import java.util.Set;

public class Schedules {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("schedules")
    private Set<Schedule> schedules = new LinkedHashSet<Schedule>();

    public Set<Schedule> getSchedules() {
        return this.schedules;
    }

    public void setSchedules(Set<Schedule> schedules) {
        this.schedules = schedules;
    }

    public final Schedules add(final Schedule schedule) {
        this.schedules.add(schedule);
        return this;
    }

    @Override
    public String toString() {
        return "Schedules{" +
                "schedules=" + this.schedules +
                '}';
    }
}
