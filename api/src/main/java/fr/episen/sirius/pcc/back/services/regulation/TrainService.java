package fr.episen.sirius.pcc.back.services.regulation;
import fr.episen.sirius.pcc.back.models.regulation.*;
import fr.episen.sirius.pcc.back.repositories.regulation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.util.*;

@Service
@Slf4j
public class TrainService {

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private TrajetRepository trajetRepository;

    @Autowired
    private HoraireRepository horaireRepository;

    @Autowired
    private ElementVoieRepository elementVoieRepository;

    @Autowired
    private FrequenceRepository frequenceRepository;

    private static final float DISTANCE_STATION = 2.0f;
    private static final float VITESSE_MAX = 80.0f;

    public List<Train> getAllTrains() {
        return trainRepository.findAll();
    }

    public Optional<Train> getTrainById(Long id) {
        return trainRepository.findById(id);
    }

    /**
     * Mise à jour toutes les 20 secondes
     */
    @Scheduled(fixedRate = 20000)
    @Transactional
    public void updateAllTrainPositions() {
        Date maintenant = new Date();
        log.info("\n=== POSITIONS {} ===", maintenant);

        List<Trajet> trajets = trajetRepository.findAll();
        int total = 0;

        for (Trajet trajet : trajets) {
            if (trajet.getTrain() == null) continue;

            List<Horaire> horaires = horaireRepository.findByTrajetOrderByDateArriveeTheorique(trajet.getId());
            if (horaires.isEmpty()) continue;

            if (updateTrain(trajet, horaires, maintenant)) {
                total++;
            }
        }

        log.info("\nTotal: {} trains\n", total);
    }

    private boolean updateTrain(Trajet trajet, List<Horaire> horaires, Date maintenant) {
        Train train = trajet.getTrain();
        Ligne ligne = trajet.getLigne();

        // Trouver où est le train
        for (int i = 0; i < horaires.size(); i++) {
            Horaire horaire = horaires.get(i);
            Date arrivee = horaire.getDateArriveeTheorique();
            Date depart = horaire.getDateDepartTheorique();

            // si Train à l'arrêt
            if (maintenant.compareTo(arrivee) >= 0 && maintenant.compareTo(depart) <= 0) {
                afficherTrain(train, ligne, horaire, 0.0f, maintenant);
                return true;
            }

            // si en mouvement
            if (i < horaires.size() - 1) {
                Horaire prochainHoraire = horaires.get(i + 1);
                Date prochaineArrivee = prochainHoraire.getDateArriveeTheorique();

                if (maintenant.compareTo(depart) > 0 && maintenant.compareTo(prochaineArrivee) < 0) {
                    long duree = prochaineArrivee.getTime() - depart.getTime();
                    float vitesse = calculerVitesse(duree);
                    afficherTrain(train, ligne, horaire, vitesse, maintenant);
                    return true;
                }
            }
        }

        return false;
    }

    private void afficherTrain(Train train, Ligne ligne, Horaire horaire, float vitesse, Date maintenant) {
        LigneStation ligneStation = horaire.getLigneStation();
        Station station = ligneStation.getStation();

        Optional<ElementVoie> elementVoieOpt = elementVoieRepository.findByLigneStationId(ligneStation.getId());
        if (elementVoieOpt.isEmpty()) return;

        ElementVoie elementVoie = elementVoieOpt.get();
        String typePeriode = getTypePeriode(ligne, maintenant);

        train.setPosition(elementVoie);
        train.setVitesse(vitesse);
        train.setDateArriveePosition(maintenant);
        trainRepository.save(train);

        log.info("Train {} | Station: {} | Element voie: {} | Vitesse: {} km/h | {}",
                train.getId(),
                station.getNom(),
                elementVoie.getId(),
                String.format("%.1f", vitesse),
                typePeriode);
    }

    private float calculerVitesse(long dureeMs) {
        if (dureeMs <= 0) return 0;
        float heures = dureeMs / 3600000.0f;
        float vitesse = DISTANCE_STATION / heures;
        return Math.min(vitesse, VITESSE_MAX);
    }

    private String getTypePeriode(Ligne ligne, Date maintenant) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(maintenant);

            int jourSemaine = cal.get(Calendar.DAY_OF_WEEK);
            long jourId = (jourSemaine == 1) ? 7 : jourSemaine - 1;

            @SuppressWarnings("deprecation")
            Time heureActuelle = new Time(
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    cal.get(Calendar.SECOND)
            );

            List<Frequence> frequences = frequenceRepository.findFrequenceApplicable(
                    ligne,
                    jourId,
                    maintenant,
                    heureActuelle
            );

            if (!frequences.isEmpty()) {
                Frequence freq = frequences.get(0);
                int recurrence = freq.getRecurrence();

                if (recurrence <= 2) return "HEURE_POINTE";
                if (recurrence <= 4) return "HEURE_NORMALE";
                if (recurrence <= 5) return "HEURE_WEEKEND";
                return "HEURE_CREUSE";
            }
        } catch (Exception e) {

        }
        return "INCONNU";
    }
}