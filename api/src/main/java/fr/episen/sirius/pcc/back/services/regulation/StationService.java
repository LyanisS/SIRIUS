package fr.episen.sirius.pcc.back.services.regulation;

import fr.episen.sirius.pcc.back.models.regulation.Station;
import fr.episen.sirius.pcc.back.repositories.regulation.StationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class StationService {
    @Autowired
    private StationRepository stationRepository;

    /**
     * Récupère la liste de toutes les stations
     * @return Liste des stations
     */
    public List<Station> getAllStations() {
        log.info("Récupération de toutes les stations");
        List<Station> stations = stationRepository.findAll();
        log.info("Nombre de stations trouvées: {}", stations.size());
        return stations;
    }

    /**
     * Récupère une station par son ID
     * @param id L'ID de la station
     * @return Optional contenant la station si elle existe
     */
    public Optional<Station> getStationById(Long id) {
        log.info("Récupération de la station avec l'ID: {}", id);
        return stationRepository.findById(id);
    }
}