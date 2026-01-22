package fr.episen.sirius.pcc.back.controllers.regulation;

import fr.episen.sirius.pcc.back.dto.regulation.CreateIncidentDTO;
import fr.episen.sirius.pcc.back.dto.regulation.UpdateIncidentDTO;
import fr.episen.sirius.pcc.back.models.regulation.Incident;
import fr.episen.sirius.pcc.back.services.regulation.IncidentService;

import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("incidents")
@CrossOrigin(origins = "*")
@Slf4j
public class IncidentController {
    @Autowired
    private IncidentService incidentService;

    /**
     * GET /api/incidents
     * Récupère les incidents
     */
    @GetMapping("")
    public ResponseEntity<List<Incident>> getIncidents(@RequestParam(required = false) Boolean today) {
        log.info("GET /api/incidents?today={}", today);
        List<Incident> incidents;
        if (today != null && today) {
            incidents = incidentService.getTodayIncidents();
        } else {
            incidents = incidentService.getAllIncidents();
        }

        return ResponseEntity.ok(incidents);
    }

    /**
     * POST /api/incidents
     * Créer un incident
     */
    @PostMapping("")
    public ResponseEntity<Incident> createIncident(@Valid @RequestBody CreateIncidentDTO dto) {
        log.info("POST /api/incidents");
        Optional<Incident> incident = incidentService.createIncident(dto);
        return incident.map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    /**
     * GET /api/incidents/{id}
     * Récupère un incident par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Incident> getIncidentById(@PathVariable Long id) {
        log.info("GET /api/incidents/{}", id);
        Optional<Incident> incident = incidentService.getIncidentById(id);
        return incident.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PATCH /api/incidents/{id}
     * Modifie un incident par son ID
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Incident> updateIncidentById(@PathVariable Long id, @Valid @RequestBody UpdateIncidentDTO dto) {
        log.info("PATCH /api/incidents/{}", id);
        Optional<Incident> incident = incidentService.updateIncidentById(id, dto);
        return incident.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Long> deleteIncidentById(@PathVariable Long id){
        log.info("DELETE /api/incidents/{}", id);
        boolean isRemoved = incidentService.deleteIncident(id);
        if(!isRemoved){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return  new ResponseEntity<>(id, HttpStatus.OK);
    }
}
