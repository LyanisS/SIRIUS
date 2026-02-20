package fr.episen.sirius.pcc.back.models.voyageur;

import fr.episen.sirius.pcc.back.models.regulation.Station;
import java.util.List;

public class ItineraireResult {
    public Station stationDepart;
    public Station stationArrivee;
    public List<PointDePassage> pointsDePassage;
    public int nombreChangements;
    public int nombreStations;
}