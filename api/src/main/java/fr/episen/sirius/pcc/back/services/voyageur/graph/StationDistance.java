package fr.episen.sirius.pcc.back.services.voyageur.graph;

public class StationDistance implements Comparable<StationDistance> {

    public Long stationId;
    public int distance;

    public StationDistance(Long stationId, int distance) {
        this.stationId = stationId;
        this.distance = distance;
    }

    @Override
    public int compareTo(StationDistance other) {
        return Integer.compare(distance, other.distance);
    }
}