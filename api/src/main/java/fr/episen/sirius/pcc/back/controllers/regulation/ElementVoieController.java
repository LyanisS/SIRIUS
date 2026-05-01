package fr.episen.sirius.pcc.back.controllers.regulation;

import fr.episen.sirius.pcc.back.models.regulation.ElementVoie;
import fr.episen.sirius.pcc.back.repositories.regulation.ElementVoieRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("elementVoies")
@CrossOrigin(origins = "*")
@Slf4j
public class ElementVoieController {

    @Autowired
    private ElementVoieRepository elementVoieRepository;

    @GetMapping("/ligne/{ligneId}")
    public ResponseEntity<List<ElementVoie>> getByLigne(@PathVariable Long ligneId) {
        log.info("GET /api/elementVoies/ligne/{}", ligneId);
        List<ElementVoie> elementVoies = elementVoieRepository.findByLigneStationLigneId(ligneId);
        return ResponseEntity.ok(elementVoies);
    }
}
