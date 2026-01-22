package fr.episen.sirius.pcc.back.services.regulation;

import fr.episen.sirius.pcc.back.dto.regulation.CreateTrajetDTO;
import fr.episen.sirius.pcc.back.dto.regulation.UpdateTrajetDTO;
import fr.episen.sirius.pcc.back.models.regulation.Ligne;
import fr.episen.sirius.pcc.back.models.regulation.Train;
import fr.episen.sirius.pcc.back.models.regulation.Trajet;
import fr.episen.sirius.pcc.back.models.regulation.Trajet;
import fr.episen.sirius.pcc.back.repositories.regulation.LigneRepository;
import fr.episen.sirius.pcc.back.repositories.regulation.TrainRepository;
import fr.episen.sirius.pcc.back.repositories.regulation.TrajetRepository;
import fr.episen.sirius.pcc.back.repositories.regulation.TrajetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TrajetService {
    @Autowired
    private TrajetRepository trajetRepository;

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private LigneRepository ligneRepository;

    /**
     * Récupère la liste de tous les trajets
     * @return Liste des trajets
     */
    public List<Trajet> getAllTrajets() {
        log.info("Récupération de tous les trajets");
        List<Trajet> trajets = trajetRepository.findAll();
        log.info("Nombre de trajets trouvées: {}", trajets.size());
        return trajets;
    }

    public List<Trajet> getActiveTrajets() {
        log.info("Récupération des trajets en cours");
        List<Trajet> trajets = trajetRepository.findActiveTrajets();
        log.info("Nombre de trajets trouvées: {}", trajets.size());
        return trajets;
    }

    public Optional<Trajet> getTrajetById(Long id) {
        log.info("Récupération de la station avec l'ID: {}", id);
        return trajetRepository.findById(id);
    }

    public Optional<Trajet> createTrajet(CreateTrajetDTO dto) {
        Trajet trajet = new Trajet();

        Optional<Ligne> ligne = ligneRepository.findById(dto.getLigneId());
        // TODO: renvoyer une erreur spécifique pour ce cas
        if (ligne.isEmpty()) return Optional.empty();

        trajet.setLigne(ligne.get());

        Optional<Train> train = trainRepository.findById(dto.getTrainId());
        // TODO: renvoyer une erreur spécifique pour ce cas
        if (train.isEmpty()) return Optional.empty();

        trajet.setTrain(train.get());

        return Optional.of(trajetRepository.save(trajet));
    }

    public Optional<Trajet> updateTrajetById(Long id, UpdateTrajetDTO dto) {
        Optional<Trajet> trajetOptional = trajetRepository.findById(id);
        if (trajetOptional.isEmpty()) return Optional.empty();

        Trajet trajet = trajetOptional.get();

        if (dto.getLigneId() != null) {
            Optional<Ligne> ligne = ligneRepository.findById(dto.getLigneId());
            // TODO: renvoyer une erreur spécifique pour ce cas
            if (ligne.isEmpty()) return Optional.empty();
            trajet.setLigne(ligne.get());
        }

        if (dto.getTrainId() != null) {
            Optional<Train> train = trainRepository.findById(dto.getTrainId());
            // TODO: renvoyer une erreur spécifique pour ce cas
            if (train.isEmpty()) return Optional.empty();
            trajet.setTrain(train.get());
        }

        return Optional.of(trajetRepository.save(trajet));
    }

    public boolean deleteTrajet(Long id) {
        Optional<Trajet> trajetOptional = trajetRepository.findById(id);
        if (trajetOptional.isPresent()) {
            trajetOptional.ifPresent(trajet -> trajetRepository.delete(trajet));
            return true;
        }
        return false;
    }
}
