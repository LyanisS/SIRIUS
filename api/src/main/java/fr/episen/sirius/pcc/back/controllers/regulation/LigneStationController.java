package fr.episen.sirius.pcc.back.controllers.regulation;

import fr.episen.sirius.pcc.back.models.regulation.Ligne;
import fr.episen.sirius.pcc.back.models.regulation.LigneStation;
import fr.episen.sirius.pcc.back.repositories.regulation.LigneRepository;
import fr.episen.sirius.pcc.back.repositories.regulation.LigneStationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("ligneStations")
@CrossOrigin(origins = "*")
@Slf4j
public class LigneStationController {

    @Autowired
    private LigneStationRepository ligneStationRepository;

    @Autowired
    private LigneRepository ligneRepository;

    @GetMapping("/ligne/{id}/asc")
    public ResponseEntity<List<LigneStation>> getByLigneAsc(@PathVariable Long id) {
        log.info("GET /api/ligneStations/ligne/{}/asc", id);
        Optional<Ligne> ligne = ligneRepository.findById(id);
        if (ligne.isEmpty()) return ResponseEntity.notFound().build();
        List<LigneStation> stations = ligneStationRepository.findByLigneOrderByOrdreAsc(ligne.get());
        return ResponseEntity.ok(stations);
    }

    @GetMapping("/ligne/{id}/desc")
    public ResponseEntity<List<LigneStation>> getByLigneDesc(@PathVariable Long id) {
        log.info("GET /api/ligneStations/ligne/{}/desc", id);
        Optional<Ligne> ligne = ligneRepository.findById(id);
        if (ligne.isEmpty()) return ResponseEntity.notFound().build();
        List<LigneStation> stations = ligneStationRepository.findByLigneOrderByOrdreDesc(ligne.get());
        return ResponseEntity.ok(stations);
    }
}
