package fr.episen.sirius.pcc.back.controllers.regulation;

import fr.episen.sirius.pcc.back.models.regulation.Train;
import fr.episen.sirius.pcc.back.services.regulation.TrainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("trains")
@CrossOrigin(origins = "*")
@Slf4j
public class TrainController {
    @Autowired
    private TrainService trainService;

    /**
     * GET /api/trains
     * Récupère tous les trains
     */
    @GetMapping("")
    public ResponseEntity<List<Train>> getAllTrains() {
        log.info("GET /api/trains");
        List<Train> trains = trainService.getAllTrains();
        return ResponseEntity.ok(trains);
    }

    /**
     * GET /api/regulation/trains/{id}
     * Récupère un train par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Train> getTrainById(@PathVariable Long id) {
        log.info("GET /api/regulation/trains/{}", id);
        Optional<Train> train = trainService.getTrainById(id);
        return train.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
