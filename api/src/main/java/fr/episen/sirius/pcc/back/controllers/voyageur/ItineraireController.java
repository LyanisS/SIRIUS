package fr.episen.sirius.pcc.back.controllers.voyageur;

import fr.episen.sirius.pcc.back.dto.voyageur.CreateItineraireFavoriDTO;
import fr.episen.sirius.pcc.back.dto.voyageur.ItineraireFavoriDTO;
import fr.episen.sirius.pcc.back.dto.voyageur.ItineraireDTO;
import fr.episen.sirius.pcc.back.models.voyageur.Utilisateur;
import fr.episen.sirius.pcc.back.security.AuthRequired;
import fr.episen.sirius.pcc.back.services.voyageur.ItineraireService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
        log.info("GET /api/itineraires/calculer?depart={}&arrivee={}", depart, arrivee);

        if (depart.equals(arrivee)) {
            return ResponseEntity.badRequest()
                    .body("Vous êtes déjà sur place!");
        }

        ItineraireDTO resultat = itineraireService.calculerItineraire(depart, arrivee);

        if (resultat == null) {
            return ResponseEntity.badRequest()
                    .body("Aucun itinéraire trouvé");
        }

        return ResponseEntity.ok(resultat);
    }

    @GetMapping("/favoris")
    @AuthRequired
    public ResponseEntity<List<ItineraireFavoriDTO>> getAllItinerairesFavoris() {
        log.info("GET /api/itineraires/favoris");
        Utilisateur utilisateur = (Utilisateur) SecurityContextHolder.getContext()
                                                                     .getAuthentication()
                                                                     .getPrincipal();

        return ResponseEntity.ok(itineraireService.getAllItinerairesFavoris(utilisateur));
    }

    @PostMapping("/favoris")
    @AuthRequired
    public ResponseEntity<ItineraireFavoriDTO> createItineraireFavori(@RequestBody CreateItineraireFavoriDTO dto) {
        log.info("POST /api/itineraires/favoris");
        Utilisateur utilisateur = (Utilisateur) SecurityContextHolder.getContext()
                                                                     .getAuthentication()
                                                                     .getPrincipal();

        Optional<ItineraireFavoriDTO> itineraire = itineraireService.createItineraireFavori(utilisateur, dto);
        return itineraire.map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }
}
