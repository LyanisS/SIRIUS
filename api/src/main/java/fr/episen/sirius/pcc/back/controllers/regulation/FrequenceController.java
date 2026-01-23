package fr.episen.sirius.pcc.back.controllers.regulation;

import fr.episen.sirius.pcc.back.models.regulation.Frequence;
import fr.episen.sirius.pcc.back.models.regulation.Station;
import fr.episen.sirius.pcc.back.services.regulation.FrequenceService;
import fr.episen.sirius.pcc.back.services.regulation.StationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("frequences")
@CrossOrigin(origins = "*")
@Slf4j
public class FrequenceController {
    @Autowired
    private FrequenceService frequenceService;

    /**
     * GET /api/frequences/trajets
     * @param hours nombre d'heures à générer
     * Récupère toutes les fréquences utilisées lors de la génération
     */
    @GetMapping("/trajets")
    public ResponseEntity<List<Frequence>> trajets(@RequestParam int hours) {
        log.info("GET /api/frequences/trajets");
        List<Frequence> frequences = frequenceService.generateTrajets(hours);
        return ResponseEntity.ok(frequences);
    }
}
