package fr.episen.sirius.pcc.back.dto.voyageur;

import fr.episen.sirius.pcc.back.models.voyageur.ItineraireResult;
import java.util.ArrayList;
import java.util.List;

public class ItineraireSimplifie {

    public String stationDepart;
    public String stationArrivee;
    public List<String> details;
    public int nombreChangements;
    public int nombreStations;

    public ItineraireSimplifie(ItineraireResult resultat) {
        this.stationDepart = resultat.stationDepart.getNom();
        this.stationArrivee = resultat.stationArrivee.getNom();
        this.details = new ArrayList<>();

        resultat.pointsDePassage.forEach(p ->
                this.details.add(p.station.getNom())
        );

        this.nombreChangements = resultat.nombreChangements;
        this.nombreStations = resultat.nombreStations;
    }
}