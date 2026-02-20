package fr.episen.sirius.pcc.back.dto.voyageur;

import java.util.List;

public class ReponseItineraireDTO {

    private String stationDepart;
    private String stationArrivee;
    private List<String> etapes;
    private int nombreStations;
    private int nombreCorrespondances;

    public ReponseItineraireDTO(
            String stationDepart,
            String stationArrivee,
            List<String> etapes,
            int nombreStations,
            int nombreCorrespondances
    ) {
        this.stationDepart = stationDepart;
        this.stationArrivee = stationArrivee;
        this.etapes = etapes;
        this.nombreStations = nombreStations;
        this.nombreCorrespondances = nombreCorrespondances;
    }

    public String getStationDepart() { return stationDepart; }
    public String getStationArrivee() { return stationArrivee; }
    public List<String> getEtapes() { return etapes; }
    public int getNombreStations() { return nombreStations; }
    public int getNombreCorrespondances() { return nombreCorrespondances; }
}
