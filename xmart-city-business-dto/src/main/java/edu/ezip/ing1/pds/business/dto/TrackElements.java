package edu.ezip.ing1.pds.business.dto;

import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TrackElements {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("track_elements")
    private Set<TrackElement> trackElements = new LinkedHashSet<TrackElement>();

    public Set<TrackElement> getTrackElements() {
        return this.trackElements;
    }

    public void setTrackElements(Set<TrackElement> trackElements) {
        this.trackElements = trackElements;
    }

    public final TrackElements add(final TrackElement trackElement) {
        this.trackElements.add(trackElement);
        return this;
    }

    @Override
    public String toString() {
        return "TrackElements{" +
                "trackElements=" + this.trackElements +
                '}';
    }
}
