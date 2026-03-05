package fr.episen.sirius.pcc.back.dto.voyageur;

import fr.episen.sirius.pcc.back.models.regulation.Station;
import lombok.Data;

import java.util.List;

@Data
public class ItineraireDTO {

    public Station stationDepart;
    public Station stationArrivee;
    public List<EtapeItineraireDTO> etapes;
    public int nombreChangements;
    public int nombreStations;
}