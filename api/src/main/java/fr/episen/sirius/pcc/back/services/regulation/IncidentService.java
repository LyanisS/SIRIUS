package fr.episen.sirius.pcc.back.services.regulation;

import fr.episen.sirius.pcc.back.dto.regulation.CreateIncidentDTO;
import fr.episen.sirius.pcc.back.dto.regulation.UpdateIncidentDTO;
import fr.episen.sirius.pcc.back.models.regulation.Incident;
import fr.episen.sirius.pcc.back.models.regulation.Trajet;
import fr.episen.sirius.pcc.back.repositories.regulation.IncidentRepository;
import fr.episen.sirius.pcc.back.repositories.regulation.TrajetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class IncidentService {
    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private TrajetRepository trajetRepository;

    /**
     * Récupère la liste de tous les incidents
     * @return Liste des incidents
     */
    public List<Incident> getAllIncidents() {
        log.info("Récupération de toutes les incidents");
        List<Incident> incidents = incidentRepository.findAll();
        log.info("Nombre d'incidents trouvées: {}", incidents.size());
        return incidents;
    }

    public List<Incident> getTodayIncidents() {
        log.info("Récupération de toutes les incidents d'aujourd'hui");
        List<Incident> incidents = incidentRepository.findTodayIncidents();
        log.info("Nombre d'incidents trouvées: {}", incidents.size());
        return incidents;
    }

    public Optional<Incident> getIncidentById(Long id) {
        log.info("Récupération de la station avec l'ID: {}", id);
        return incidentRepository.findById(id);
    }

    public Optional<Incident> createIncident(CreateIncidentDTO dto) {
        Incident incident = new Incident();
        incident.setMessage(dto.getMessage());
        incident.setDateDebut(new Date());
        incident.setDateFin(dto.getDateFin());

        Optional<Trajet> trajet = trajetRepository.findById(dto.getTrajetId());
        if (trajet.isEmpty()) return Optional.empty();

        incident.setTrajet(trajet.get());

        return Optional.of(incidentRepository.save(incident));
    }

    public Optional<Incident> updateIncidentById(Long id, UpdateIncidentDTO dto) {
        Optional<Incident> incidentOptional = incidentRepository.findById(id);
        if (incidentOptional.isEmpty()) return Optional.empty();

        Incident incident = incidentOptional.get();

        if (dto.getMessage() != null) incident.setMessage(dto.getMessage());
        if (dto.getDateDebut() != null) incident.setDateDebut(dto.getDateDebut());
        if (dto.getDateFin() != null) incident.setDateFin(dto.getDateFin());
        if (dto.getTrajetId() != null) {
            Optional<Trajet> trajet = trajetRepository.findById(dto.getTrajetId());
            // TODO: renvoyer une erreur spécifique pour ce cas
            if (trajet.isEmpty()) return Optional.empty();
            incident.setTrajet(trajet.get());
        }

        return Optional.of(incidentRepository.save(incident));
    }

    public boolean deleteIncident(Long id) {
        Optional<Incident> incidentOptional = incidentRepository.findById(id);
        if (incidentOptional.isPresent()) {
            incidentOptional.ifPresent(incident -> incidentRepository.delete(incident));
            return true;
        }
        return false;
    }
}
