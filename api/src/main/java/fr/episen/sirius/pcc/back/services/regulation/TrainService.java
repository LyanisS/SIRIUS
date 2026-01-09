package fr.episen.sirius.pcc.back.services.regulation;
import fr.episen.sirius.pcc.back.models.regulation.*;
import fr.episen.sirius.pcc.back.repositories.regulation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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

    public List<Train> getAllTrains() {
        return trainRepository.findAll();
    }

    public Optional<Train> getTrainById(Long id) {
        return trainRepository.findById(id);
    }

    @Scheduled(fixedRate = 20000)
    @Transactional
    public void updateAllTrainPositions() {
        Date maintenant = new Date();
        log.info("\n=== Mise a jour positions a {} ===", maintenant);

        List<Trajet> trajets = trajetRepository.findAll();

        for (Trajet trajet : trajets) {
            assert trajet.getTrain() != null;

            RecupPositionTrain(trajet, maintenant);
        }
    }

    private void RecupPositionTrain(Trajet trajet, Date maintenant) {
        Train train = trajet.getTrain();

        List<Horaire> horaires = horaireRepository.findByTrajetOrderByDateArriveeTheorique(trajet.getId());

        if (horaires.isEmpty()) {
            log.warn("Train {} : Aucun horaire trouve pour le trajet {}", train.getId(), trajet.getId());
            return;
        }

        Horaire horaireActuel = RecupHorairesActuel(horaires, maintenant);

        if (horaireActuel == null) {
            log.info("Train {} : Hors service", train.getId());
            return;
        }

        LigneStation ligneStation = horaireActuel.getLigneStation();
        Optional<ElementVoie> elementVoieOpt = elementVoieRepository.findByLigneStationId(ligneStation.getId());


        ElementVoie elementVoie = elementVoieOpt.get();
        float vitesse = calculeVitesse(horaireActuel, horaires, maintenant);

        train.setPosition(elementVoie);
        train.setVitesse(vitesse);
        train.setDateArriveePosition(maintenant);
        trainRepository.save(train);

        String etat = (vitesse == 0) ? "A l'arret" : "En mouvement";
        log.info("Train {} | {} | Station: {} | ElementVoie: {} | Vitesse: {} km/h",
                train.getId(),
                etat,
                ligneStation.getStation().getNom(),
                elementVoie.getId(),
                String.format("%.1f", vitesse));
    }

    private Horaire RecupHorairesActuel(List<Horaire> horaires, Date maintenant) {
        for (int i = 0; i < horaires.size(); i++) {
            Horaire horaire = horaires.get(i);
            Date arrivee = horaire.getDateArriveeTheorique();
            Date depart = horaire.getDateDepartTheorique();

            if (maintenant.compareTo(arrivee) >= 0 && maintenant.compareTo(depart) <= 0) {
                return horaire;
            }

            if (i < horaires.size() - 1) {
                Horaire prochainHoraire = horaires.get(i + 1);
                Date prochaineArrivee = prochainHoraire.getDateArriveeTheorique();

                if (maintenant.compareTo(depart) > 0 && maintenant.compareTo(prochaineArrivee) < 0) {
                    return horaire;
                }
            }
        }

        return null;
    }

    private float calculeVitesse(Horaire horaireActuel, List<Horaire> horaires, Date maintenant) {
        int indexActuel = horaires.indexOf(horaireActuel);
        Date arrivee = horaireActuel.getDateArriveeTheorique();
        Date depart = horaireActuel.getDateDepartTheorique();

        if (maintenant.compareTo(arrivee) >= 0 && maintenant.compareTo(depart) <= 0) {
            return 0.0f;
        }

        if (indexActuel < horaires.size() - 1) {
            Horaire prochainHoraire = horaires.get(indexActuel + 1);
            long dureeTrajet = prochainHoraire.getDateArriveeTheorique().getTime() - horaireActuel.getDateDepartTheorique().getTime();
            float distanceKm = 2.0f;

            if (dureeTrajet > 0) {
                float dureeHeures = dureeTrajet / 3600000.0f;
                return distanceKm / dureeHeures;
            }
        }

        return 0.0f;
    }
}