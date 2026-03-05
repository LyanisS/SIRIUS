package fr.episen.sirius.pcc.back.dto.voyageur;

import fr.episen.sirius.pcc.back.models.regulation.Ligne;
import fr.episen.sirius.pcc.back.models.regulation.Station;
import lombok.Data;

@Data
public class EtapeItineraireDTO {
    private Station station;
    private Ligne ligne;

    public EtapeItineraireDTO() {}

    public EtapeItineraireDTO(Station station, Ligne ligne) {
        this.station = station;
        this.ligne = ligne;
    }
}