package fr.episen.sirius.pcc.back.services.voyageur.graph;

import java.util.Map;

public class DijkstraResult {

    public Map<Long, Integer> distances;
    public Map<Long, Long> precedent;

    public DijkstraResult(Map<Long, Integer> distances, Map<Long, Long> precedent) {
        this.distances = distances;
        this.precedent = precedent;
    }
}