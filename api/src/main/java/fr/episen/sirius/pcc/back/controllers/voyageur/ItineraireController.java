package fr.episen.sirius.pcc.back.controllers.voyageur;

import fr.episen.sirius.pcc.back.services.voyageur.ItineraireService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        log.info(" Requête d'itinéraire: {} → {}", depart, arrivee);

        if (depart.equals(arrivee)) {
            return ResponseEntity.badRequest()
                    .body("Vous êtes déja sur place!!!");
        }

        ItineraireService.ItineraireResult resultat =
                itineraireService.calculerItineraire(depart, arrivee);

        if (resultat == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(resultat);
    }
}
