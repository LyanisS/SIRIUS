package fr.episen.sirius.pcc.back.dto.voyageur;

public class EtapeDTO {

    private String station;
    private String ligne;

    public EtapeDTO(String station, String ligne) {
        this.station = station;
        this.ligne = ligne;
    }

    public String getStation() {
        return station;
    }

    public String getLigne() {
        return ligne;
    }
}