package fr.episen.sirius.pcc.back.controllers.voyageur;

import fr.episen.sirius.pcc.back.dto.voyageur.EtapeDTO;
import fr.episen.sirius.pcc.back.dto.voyageur.ReponseItineraireDTO;
import fr.episen.sirius.pcc.back.models.voyageur.ItineraireResult;
import fr.episen.sirius.pcc.back.services.voyageur.ItineraireService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/itineraires")
@CrossOrigin(origins = "*")
public class ItineraireController {

    @Autowired
    private ItineraireService itineraireService;

    @GetMapping("/calculer")
    public ReponseItineraireDTO calculer(
            @RequestParam Long depart,
            @RequestParam Long arrivee) {

        ItineraireResult resultat =
                itineraireService.calculerItineraire(depart, arrivee);

        List<EtapeDTO> etapes = resultat.pointsDePassage.stream()
                .map(p -> new EtapeDTO(
                        p.station.getNom(),
                        p.ligne != null ? p.ligne.getNom() : null
                ))
                .toList();

        return new ReponseItineraireDTO(
                resultat.stationDepart.getNom(),
                resultat.stationArrivee.getNom(),
                etapes,
                resultat.nombreStations,
                resultat.nombreChangements
        );
    }
}