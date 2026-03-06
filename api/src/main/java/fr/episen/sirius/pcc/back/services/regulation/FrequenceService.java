package fr.episen.sirius.pcc.back.services.regulation;

import fr.episen.sirius.pcc.back.models.regulation.*;
import fr.episen.sirius.pcc.back.repositories.regulation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class FrequenceService {
    private static final int STATION_STOP_MINUTES = 2;
    private static final double TRAIN_SPEED_KMH = 60.0;
    private static final double TRIP_LENGTH_KM = 0.6;

    @Autowired
    private FrequenceRepository frequenceRepository;

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private TrajetRepository trajetRepository;

    @Autowired
    private HoraireRepository horaireRepository;

    @Autowired
    private LigneStationRepository ligneStationRepository;
    @Autowired
    private ElementVoieRepository elementVoieRepository;

    /**
     * Génère les trajets pour les procahines heures
     * @param hours nombre d'heures à générer
     */
    @Transactional
    public List<Frequence> generateTrajets(int hours) {
        log.info("--- Démarage génération de trajets ----");

        Calendar dateDebut = Calendar.getInstance();
        Calendar dateFin = Calendar.getInstance();
        dateFin.add(Calendar.HOUR_OF_DAY, hours);

        List<Frequence> frequences = this.frequenceRepository.findActiveFrequencesForHours(hours);
        log.info("{} fréquences potentiellement applicables", frequences.size());

        if (frequences.isEmpty()) return frequences;

        int calendarDay = dateDebut.get(Calendar.DAY_OF_WEEK);
        Jour jourActuel = (calendarDay == Calendar.SUNDAY) ? Jour.DIMANCHE : Jour.getByIndex(calendarDay - 1);
        Date date = dateDebut.getTime();

        for (Frequence frequence : frequences) {
            if (frequence.getJours().isEmpty()) continue;

            List<LigneStation> stations;
            if (frequence.getSens()) {
                stations = ligneStationRepository.findByLigneOrderByOrdreAsc(frequence.getLigne());
            } else {
                stations = ligneStationRepository.findByLigneOrderByOrdreDesc(frequence.getLigne());
            }
            Map<LigneStation, ElementVoie> ligneStationElementVoieMap = new HashMap<>();
            for (int i = 0; i < stations.size(); i++) {
                if (i==0) ligneStationElementVoieMap.put(stations.get(i), null);
                else ligneStationElementVoieMap.put(
                        stations.get(i),
                        elementVoieRepository.findElementVoieBetweenStations(stations.get(i-1), stations.get(i))
                );
            }
            List<Train> trains = trainRepository.findAll();

            for (Jour jourFrequence : frequence.getJours()) {
                if (Objects.equals(jourActuel.getId(), jourFrequence.getId())) {
                    if (date.before(frequence.getDateDebut()) || date.after(frequence.getDateFin())) continue;

                    this.generateTrajet(frequence, dateDebut, dateFin, ligneStationElementVoieMap, trains);
                }
            }
        }

        return frequences;
    }

    private void generateTrajet(Frequence frequence, Calendar dateDebut, Calendar dateFin, Map<LigneStation, ElementVoie> ligneStationElementVoieMap, List<Train> trains) {
        Calendar dateProchainTrajet = (Calendar) dateDebut.clone();

        while (!dateProchainTrajet.after(dateFin)) {
            // TODO: améliorer la façon de faire le choix du train pour le trajet pour éviter d'assigner un train actif
            Train train = trains.get(ThreadLocalRandom.current().nextInt(trains.size()));

            Trajet trajet = new Trajet();
            trajet.setLigne(frequence.getLigne());
            trajet.setTrain(train);
            trajet = trajetRepository.save(trajet);

            this.createHorairesForTrajet(trajet, ligneStationElementVoieMap, dateProchainTrajet.getTime());
            dateProchainTrajet.add(Calendar.MINUTE, frequence.getRecurrence());
        }
    }

    private void createHorairesForTrajet(Trajet trajet, Map<LigneStation, ElementVoie> ligneStationElementVoieMap, Date startTime) {
        Calendar dateTrajet = Calendar.getInstance();
        dateTrajet.setTime(startTime);

        log.info("- Création de {} horaires pour le trajet n°{}  ({})", ligneStationElementVoieMap.size(), trajet.getId(), startTime);

        int i = 0;
        for (Map.Entry<LigneStation, ElementVoie> entry : ligneStationElementVoieMap.entrySet()) {
            LigneStation station = entry.getKey();

            Horaire horaire = new Horaire();
            horaire.setTrajet(trajet);
            horaire.setLigneStation(station);

            if (i == 0) {
                horaire.setDateDepartTheorique(dateTrajet.getTime());
                horaire.setDateArriveeTheorique(dateTrajet.getTime());
                log.info("   Station 1/{} : {} - Départ prévu à {}", ligneStationElementVoieMap.size(), station.getStation().getNom(),
                        dateTrajet.getTime());
            } else {
                ElementVoie elementVoie = entry.getValue();
                double distanceKm = TRIP_LENGTH_KM;
                if (elementVoie != null) distanceKm = (double) elementVoie.getLongueur() / 1000;
                int dureeVoyage = (int) Math.ceil((distanceKm / TRAIN_SPEED_KMH) * 60);
                dateTrajet.add(Calendar.MINUTE, dureeVoyage);
                horaire.setDateArriveeTheorique(dateTrajet.getTime());

                if (i < ligneStationElementVoieMap.size() - 1) {
                    dateTrajet.add(Calendar.MINUTE, STATION_STOP_MINUTES);
                    horaire.setDateDepartTheorique(dateTrajet.getTime());
                    log.info("   Station {}/{}: {} - Arrivé à {}, Départ à {} (+{}min de trajet)", i + 1,
                            ligneStationElementVoieMap.size(), station.getStation().getNom(), horaire.getDateArriveeTheorique(),
                            horaire.getDateDepartTheorique(), dureeVoyage);
                } else {
                    horaire.setDateDepartTheorique(dateTrajet.getTime());
                    log.info("   Station {}/{}: {} - Arrivée au terminus à {}", i + 1, ligneStationElementVoieMap.size(),
                            station.getStation().getNom(), dateTrajet.getTime());
                }
            }

            horaireRepository.save(horaire);

            i++;
        }
    }
}
