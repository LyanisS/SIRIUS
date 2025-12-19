package fr.episen.sirius.pcc.back.services.regulation;

import fr.episen.sirius.pcc.back.models.regulation.Train;
import fr.episen.sirius.pcc.back.models.regulation.Station;
import fr.episen.sirius.pcc.back.repositories.regulation.TrainRepository;
import fr.episen.sirius.pcc.back.repositories.regulation.StationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Slf4j
public class TrainService {

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private StationService stationService;

    private Random random = new Random();

    /**
     * Récupère la liste de tous les trains
     * @return Liste des trains
     */
    public List<Train> getAllTrains() {
        log.info("Récupération de tous les trains");
        List<Train> trains = trainRepository.findAll();
        log.info("Nombre de trains trouvés: {}", trains.size());
        return trains;
    }

    /**
     * Récupère un train par son ID
     * @param id L'ID du train
     * @return Optional contenant le train s'il existe
     */
    public Optional<Train> getTrainById(Long id) {
        log.info("Récupération du train avec l'ID: {}", id);
        return trainRepository.findById(id);
    }

    /**
     * Initialisation au démarrage
     */
    @PostConstruct
    public void init() {
        log.info("=== TrainService initialisé ===");
        log.info("Simulation de mouvement des trains démarrée - Mise à jour toutes les 10 secondes");
    }

    /**
     * Simulation : Affecte aléatoirement une station à chaque train toutes les 10 secondes
     */
    @Scheduled(fixedRate = 10000) // 10000 ms = 10 secondes
    public void simulateTrainMovement() {
        List<Train> trains = this.getAllTrains();
        List<Station> stations = stationService.getAllStations();

        // Vérification qu'il y a des trains et des stations
        if (trains.isEmpty()) {
            log.warn("Aucun train trouvé pour la simulation");
            return;
        }

        if (stations.isEmpty()) {
            log.warn("Aucune station trouvée pour la simulation");
            return;
        }

        log.info("=== SIMULATION - Mise à jour de la position des trains ===");

        // Boucle sur chaque train
        for (Train train : trains) {
            // Sélection aléatoire d'une station
            Station randomStation = stations.get(random.nextInt(stations.size()));

            // Génération d'une vitesse aléatoire entre 60 et 100 km/h
            float newVitesse = 60 + random.nextFloat() * 40;

            // Mise à jour du train
            train.setVitesse(newVitesse);
            train.setDateArriveePosition(new Date());

            // Sauvegarde en base
            trainRepository.save(train);

            log.info("Train ID {} | Station: '{}' | Vitesse: {:.2f} km/h | Date: {}",
                    train.getId(),
                    randomStation.getNom(),
                    newVitesse,
                    train.getDateArriveePosition());
        }

        log.info("=== Fin de la mise à jour - {} trains déplacés ===", trains.size());
    }
}