package edu.ezip.ing1.pds.business.dto;

import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

public class Schedules {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonValue
    private Set<Schedule> schedules = new LinkedHashSet<>();

    public Set<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(Set<Schedule> schedules) {
        this.schedules = schedules;
    }

    public final Schedules add(final Schedule schedule) {
        schedules.add(schedule);
        return this;
    }

    @Override
    public String toString() {
        return "Schedules{"
                + "schedules=" + schedules
                + '}';
    }
}
