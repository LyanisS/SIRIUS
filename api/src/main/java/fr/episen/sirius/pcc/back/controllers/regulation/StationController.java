package fr.episen.sirius.pcc.back.controllers.regulation;

import fr.episen.sirius.pcc.back.models.regulation.Station;
import fr.episen.sirius.pcc.back.services.regulation.StationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("stations")
@CrossOrigin(origins = "*")
@Slf4j
public class StationController {
    @Autowired
    private StationService stationService;

    /**
     * GET /api/stations
     * Récupère toutes les stations
     */
    @GetMapping("")
    public ResponseEntity<List<Station>> getAllStations() {
        log.info("GET /api/stations");
        List<Station> stations = stationService.getAllStations();
        return ResponseEntity.ok(stations);
    }

    /**
     * GET /api/stations/{id}
     * Récupère une station par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Station> getStationById(@PathVariable Long id) {
        log.info("GET /api/stations/{}", id);
        Optional<Station> station = stationService.getStationById(id);
        return station.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
