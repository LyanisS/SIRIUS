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
        List<Train> trains = trainRepository.findAll();
        List<Trajet> trajets = trajetRepository.findAll();
        Map<Long, Boolean> sensTrain = new HashMap<>();
        for (Trajet t : trajets) {
            if (t.getTrain() != null && t.getSens() != null) {
                sensTrain.put(t.getTrain().getId(), t.getSens());
            }
        }
        for (Train train : trains) {
            train.setSens(sensTrain.get(train.getId()));
        }
        return trains;
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
        try {
            List<Trajet> trajets = trajetRepository.findAll();
            for (Trajet trajet : trajets) {
                if (trajet != null) {
                    updateTrain(trajet);
                }
            }
        } catch (Exception e) {
            log.debug("Erreur mise à jour positions: {}", e.getMessage());
        }
    }

    private void updateTrain(Trajet trajet) {
        try {
            Train train = trajet.getTrain();
            if (train == null) {
                return;
            }

            Ligne ligne = trajet.getLigne();
            List<Horaire> horairesTrajet = horaireRepository.findByTrajetOrderByDateArriveeTheorique(trajet.getId());

            if (horairesTrajet.isEmpty()) {
                return;
            }

            Date maintenant = new Date();

            // Déterminer le sens à partir de l'ordre des horaires
            boolean sensActuel = true;
            if (horairesTrajet.size() >= 2) {
                int premierOrdre = horairesTrajet.get(0).getLigneStation().getOrdre();
                int dernierOrdre = horairesTrajet.get(horairesTrajet.size() - 1).getLigneStation().getOrdre();
                sensActuel = premierOrdre <= dernierOrdre;
            }
            trajet.setSens(sensActuel);
            trajetRepository.save(trajet);

            // Trouver où est le train
            for (int i = 0; i < horairesTrajet.size(); i++) {
                Horaire horaire = horairesTrajet.get(i);

                if (horaire == null || horaire.getDateArriveeTheorique() == null || horaire.getDateDepartTheorique() == null) {
                    continue;
                }

                Date arrivee = horaire.getDateArriveeTheorique();
                Date depart = horaire.getDateDepartTheorique();

            // si Train à l'arrêt
            if (maintenant.compareTo(arrivee) >= 0 && maintenant.compareTo(depart) <= 0) {
                afficherTrain(train, ligne, horaire, 0.0f, maintenant, sensActuel);
                return;
            }

                // si en mouvement
                if (i < horairesTrajet.size() - 1) {
                    Horaire prochainHoraire = horairesTrajet.get(i + 1);

                    if (prochainHoraire != null && prochainHoraire.getDateArriveeTheorique() != null) {
                        Date prochaineArrivee = prochainHoraire.getDateArriveeTheorique();

                        if (maintenant.compareTo(depart) > 0 && maintenant.compareTo(prochaineArrivee) < 0) {
                            long duree = prochaineArrivee.getTime() - depart.getTime();
                            float vitesse = calculerVitesse(duree);
                            afficherTrain(train, ligne, horaire, vitesse, maintenant, sensActuel);
                            return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Erreur mise à jour position pour trajet: {}", e.getMessage());
        }
    }

    private void afficherTrain(Train train, Ligne ligne, Horaire horaire, float vitesse, Date maintenant, boolean sens) {
        LigneStation ligneStation = horaire.getLigneStation();
        Station station = ligneStation.getStation();

        List<ElementVoie> tousLesElementVoies = elementVoieRepository.findByLigneStationId(ligneStation.getId());

       Set<ElementVoie> elementVoiesDisponibles = new HashSet<>(tousLesElementVoies);
       List<Train> tousLesTrains = trainRepository.findAll();
       
       for (Train trainExistant : tousLesTrains) {
                if (trainExistant == null || trainExistant.getId().equals(train.getId())) {
                    continue;
                }

        if (trainExistant.getPosition() != null) {
            elementVoiesDisponibles.removeIf(ev -> ev.getId().equals(trainExistant.getPosition().getId()));
                }
            }


       ElementVoie elementVoieChoisi;
       if (!elementVoiesDisponibles.isEmpty()) {
           elementVoieChoisi = elementVoiesDisponibles.iterator().next();
       } else if (!tousLesElementVoies.isEmpty()) {
           elementVoieChoisi = tousLesElementVoies.get(0);
       } else {
           log.warn("Aucun élément de voie pour station {} (ligneStation {})", station.getNom(), ligneStation.getId());
           return;
       }

       String typePeriode = getTypePeriode(ligne, maintenant);

        train.setPosition(elementVoieChoisi);
        train.setVitesse(vitesse);
        train.setDateArriveePosition(maintenant);
        trainRepository.save(train);

        log.info("Train {} | Station: {} | Element voie: {} | Vitesse: {} km/h | {}",
                train.getId(),
                station.getNom(),
                elementVoieChoisi.getId(),
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