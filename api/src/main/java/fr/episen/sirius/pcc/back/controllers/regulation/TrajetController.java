package fr.episen.sirius.pcc.back.controllers.regulation;

import fr.episen.sirius.pcc.back.dto.regulation.CreateTrajetDTO;
import fr.episen.sirius.pcc.back.dto.regulation.UpdateTrajetDTO;
import fr.episen.sirius.pcc.back.models.regulation.Trajet;
import fr.episen.sirius.pcc.back.services.regulation.TrajetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("trajets")
@CrossOrigin(origins = "*")
@Slf4j
public class TrajetController {
    @Autowired
    private TrajetService trajetService;

    /**
     * GET /api/trajets
     * Récupère les trajets
     */
    @GetMapping("")
    public ResponseEntity<List<Trajet>> getTrajets(@RequestParam(required = false) String statut) {
        log.info("GET /api/trajets?statut={}", statut);
        List<Trajet> trajets;
        if (statut != null && statut.equalsIgnoreCase("actif")) {
            trajets = trajetService.getActiveTrajets();
        } else {
            trajets = trajetService.getAllTrajets();
        }
        return ResponseEntity.ok(trajets);
    }

    /**
     * POST /api/trajets
     * Créer un trajet
     */
    @PostMapping("")
    public ResponseEntity<Trajet> createTrajet(@Valid @RequestBody CreateTrajetDTO dto) {
        log.info("POST /api/trajets");
        Optional<Trajet> trajet = trajetService.createTrajet(dto);
        return trajet.map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    /**
     * GET /api/trajets/{id}
     * Récupère un trajet par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Trajet> getTrajetById(@PathVariable Long id) {
        log.info("GET /api/trajets/{}", id);
        Optional<Trajet> trajet = trajetService.getTrajetById(id);
        return trajet.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PATCH /api/trajets/{id}
     * Modifie un trajet par son ID
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Trajet> updateTrajetById(@PathVariable Long id, @Valid @RequestBody UpdateTrajetDTO dto) {
        log.info("PATCH /api/trajets/{}", id);
        Optional<Trajet> trajet = trajetService.updateTrajetById(id, dto);
        return trajet.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Long> deleteTrajetById(@PathVariable Long id){
        log.info("DELETE /api/trajets/{}", id);
        boolean isRemoved = trajetService.deleteTrajet(id);
        if(!isRemoved){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return  new ResponseEntity<>(id, HttpStatus.OK);
    }
}
