package fr.episen.sirius.pcc.back.models.voyageur;

import fr.episen.sirius.pcc.back.models.regulation.Ligne;
import fr.episen.sirius.pcc.back.models.regulation.Station;

public class PointDePassage {
    public Station station;
    public Ligne ligne;

    public PointDePassage(Station station, Ligne ligne) {
        this.station = station;
        this.ligne = ligne;
    }
}