package fr.episen.sirius.pcc.back.controllers.voyageur;

import fr.episen.sirius.pcc.back.dto.voyageur.ItineraireSimplifie;
import fr.episen.sirius.pcc.back.services.voyageur.ItineraireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/itineraires")
@CrossOrigin(origins = "*")
public class ItineraireController {

    @Autowired
    private ItineraireService itineraireService;

    @GetMapping("/calculer")
    public ResponseEntity<?> calculerItineraire(
            @RequestParam Long depart,
            @RequestParam Long arrivee) {

        if (depart.equals(arrivee)) {
            return ResponseEntity.badRequest()
                    .body("Vous êtes déjà sur place!");
        }

        ItineraireResult resultat = itineraireService.calculerItineraire(depart, arrivee);

        if (resultat == null) {
            return ResponseEntity.badRequest()
                    .body("Aucun itinéraire trouvé");
        }

        return ResponseEntity.ok(new ItineraireSimplifie(resultat));
    }
}
