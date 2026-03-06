package fr.episen.sirius.pcc.back.controllers.regulation;

import fr.episen.sirius.pcc.back.models.regulation.Ligne;
import fr.episen.sirius.pcc.back.services.regulation.LigneService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("lignes")
@CrossOrigin(origins = "*")
@Slf4j
public class LigneController {
    @Autowired
    private LigneService ligneService;

    /**
     * GET /api/lignes
     * Récupère toutes les lignes
     */
    @GetMapping("")
    public ResponseEntity<List<Ligne>> getAllLignes() {
        log.info("GET /api/lignes");
        List<Ligne> lignes = ligneService.getAllLignes();
        return ResponseEntity.ok(lignes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ligne> getLigneById(@PathVariable Long id) {
        log.info("GET /api/lignes/{}", id);
        Optional<Ligne> ligne = ligneService.getLigneById(id);
        return ligne.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
