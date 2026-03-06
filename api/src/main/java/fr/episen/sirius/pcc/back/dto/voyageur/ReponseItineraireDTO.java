package fr.episen.sirius.pcc.back.dto.voyageur;

import java.util.List;

public class ReponseItineraireDTO {

    private String stationDepart;
    private String stationArrivee;
    private List<EtapeDTO> details;
    private int nombreStations;
    private int nombreChangements;

    public ReponseItineraireDTO(
            String stationDepart,
            String stationArrivee,
            List<EtapeDTO> details,
            int nombreStations,
            int nombreChangements) {

        this.stationDepart = stationDepart;
        this.stationArrivee = stationArrivee;
        this.details = details;
        this.nombreStations = nombreStations;
        this.nombreChangements = nombreChangements;
    }
    public String getStationDepart() {
        return stationDepart;
    }
    public String getStationArrivee() {
        return stationArrivee;
    }
    public List<EtapeDTO> getDetails() {
        return details;
    }
    public int getNombreStations() {
        return nombreStations;
    }
    public int getNombreChangements() {
        return nombreChangements;
    }
}