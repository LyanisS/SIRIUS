package fr.episen.sirius.pcc.back.dto.voyageur;


import fr.episen.sirius.pcc.back.models.regulation.Station;
import fr.episen.sirius.pcc.back.models.voyageur.Itineraire;
import lombok.Data;

import java.util.Date;

@Data
public class ItineraireFavoriDTO {
    private Long id;

    private Date date;

    private boolean depart;

    private Station stationDepart;

    private Station stationArrivee;

    public ItineraireFavoriDTO() {}

    public ItineraireFavoriDTO(Itineraire itineraire) {
        this.id = itineraire.getId();
        this.date = itineraire.getDate();
        this.depart = itineraire.getDepart();
        this.stationDepart = itineraire.getStationDepart();
        this.stationArrivee = itineraire.getStationArrivee();
    }
}
