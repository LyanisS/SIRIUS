package fr.episen.sirius.pcc.back.services.regulation;

import fr.episen.sirius.pcc.back.models.regulation.ElementVoie;
import fr.episen.sirius.pcc.back.models.regulation.Train;
import fr.episen.sirius.pcc.back.repositories.regulation.ElementVoieRepository;
import fr.episen.sirius.pcc.back.repositories.regulation.TrainRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private ElementVoieRepository elementVoieRepository;

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

    public Optional<Train> getTrainById(Long id) {
        log.info("Récupération du train avec l'ID: {}", id);
        return trainRepository.findById(id);
    }

    @PostConstruct
    public void init() {
        log.info("Simulation de mouvement des trains démarrée - Mise à jour toutes les 10 secondes");
    }

    @Scheduled(fixedRate = 10000) // 10000 ms = 10 secondes
    @Transactional
    public void simulateTrainMovement() {
        List<Train> trains = this.getAllTrains();
        List<ElementVoie> elementVoies = elementVoieRepository.findAll();

        if (trains.isEmpty()) {
            log.warn("Aucun train trouvé pour la simulation");
            return;
        }

        if (elementVoies.isEmpty()) {
            log.warn("Aucun élément de voie trouvé pour la simulation");
            return;
        }

        log.info("=== SIMULATION - Mise à jour de la position des trains ===");

        for (Train train : trains) {
                ElementVoie randomElementVoie = elementVoies.get(random.nextInt(elementVoies.size()));
                float newVitesse = 60 + random.nextFloat() * 40;

                train.setPosition(randomElementVoie);
                train.setVitesse(newVitesse);
                train.setDateArriveePosition(new Date());

                trainRepository.save(train);

            log.info("Train ID {} | ElementVoie ID: '{}' | Vitesse: {:.2f} km/h | Date: {}",
                    train.getId(),
                    randomElementVoie.getId(),
                    newVitesse,
                    train.getDateArriveePosition());
        }

        log.info("=== Fin de la mise à jour - {} trains déplacés ===", trains.size());
        //TODO creer des trajets puis des horaires , puis sur les trajets je pourrai recup le train en question et
        // sa position cad sur quelle station et sur quel element de voie


    }
}