package fr.episen.sirius.pcc.back.controllers.voyageur;

import fr.episen.sirius.pcc.back.services.voyageur.ItineraireService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/itineraires")
@CrossOrigin(origins = "*")
@Slf4j
public class ItineraireController {

    @Autowired
    private ItineraireService itineraireService;

    @GetMapping("/calculer")
    public ResponseEntity<?> calculerItineraire(
            @RequestParam Long depart,
            @RequestParam Long arrivee) {


        if (depart.equals(arrivee)) {
            return ResponseEntity.badRequest()
                    .body("Vous êtes déja sur place!!!");
        }

        ItineraireService.ItineraireResult resultat =
                itineraireService.calculerItineraire(depart, arrivee);

        if (resultat == null) {
            return ResponseEntity
                    .badRequest()
                    .body("Cette station n'existe pas, veuillez Choisir une autre plzz !!");
        }


        ItineraireSimplifie simplifie = new ItineraireSimplifie(resultat);

        return ResponseEntity.ok(simplifie);
    }

    public static class ItineraireSimplifie {
        public String stationDepart;
        public String stationArrivee;
        public List<String> details;
        public int nombreChangements;
        public int nombreStations;

        public ItineraireSimplifie(ItineraireService.ItineraireResult resultat) {
            this.stationDepart = resultat.stationDepart.getNom();
            this.stationArrivee = resultat.stationArrivee.getNom();
            this.details = new ArrayList<>();
            for (ItineraireService.PointDePassage point : resultat.pointsDePassage) {
                this.details.add(point.station.getNom());
            }
            this.nombreChangements = resultat.nombreChangements;
            this.nombreStations = resultat.nombreStations;
        }
    }
}
