package fr.episen.sirius.pcc.back.services.voyageur.graph;

public class Voisin {

    public Long stationId;
    public int distance;
    public Long ligneId;

    public Voisin(Long stationId, int distance) {
        this.stationId = stationId;
        this.distance = distance;
    }
}