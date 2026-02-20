package fr.episen.sirius.pcc.back.services.voyageur;

import fr.episen.sirius.pcc.back.models.regulation.Ligne;
import fr.episen.sirius.pcc.back.models.regulation.LigneStation;
import fr.episen.sirius.pcc.back.models.regulation.Station;
import fr.episen.sirius.pcc.back.models.voyageur.ItineraireResult;
import fr.episen.sirius.pcc.back.models.voyageur.PointDePassage;
import fr.episen.sirius.pcc.back.repositories.regulation.LigneStationRepository;
import fr.episen.sirius.pcc.back.repositories.regulation.StationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ItineraireService {

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private LigneStationRepository ligneStationRepository;

    public ItineraireResult calculerItineraire(Long stationDepartId, Long stationArriveeId) {

        Station depart = stationRepository.findById(stationDepartId).orElse(null);
        Station arrivee = stationRepository.findById(stationArriveeId).orElse(null);

        if (depart == null || arrivee == null) {
            log.error("Station introuvable");
            return null;
        }

        List<LigneStation> lignesDepart = ligneStationRepository.findByStation(depart);
        if (lignesDepart.isEmpty()) {
            return null;
        }

        ItineraireResult meilleur = null;

        for (LigneStation ls : lignesDepart) {
            Ligne ligne = ls.getLigne();

            ItineraireResult direct = chercherTrajetDirect(depart, arrivee, ligne);
            if (direct != null && (meilleur == null || direct.nombreStations < meilleur.nombreStations)) {
                meilleur = direct;
            }

            ItineraireResult avecChangement = chercherAvecUnChangement(depart, arrivee, ligne);
            if (avecChangement != null && (meilleur == null || avecChangement.nombreStations < meilleur.nombreStations)) {
                meilleur = avecChangement;
            }
        }

        return meilleur;
    }

    private ItineraireResult chercherTrajetDirect(Station depart, Station arrivee, Ligne ligne) {

        List<LigneStation> stations = ligneStationRepository.findByLigneOrderByOrdre(ligne);

        int indexDepart = trouverPosition(stations, depart);
        int indexArrivee = trouverPosition(stations, arrivee);

        if (indexDepart == -1 || indexArrivee == -1) {
            return null;
        }

        ItineraireResult resultat = new ItineraireResult();
        resultat.stationDepart = depart;
        resultat.stationArrivee = arrivee;
        resultat.pointsDePassage = new ArrayList<>();
        resultat.nombreChangements = 0;

        int debut = Math.min(indexDepart, indexArrivee);
        int fin = Math.max(indexDepart, indexArrivee);

        for (int i = debut; i <= fin; i++) {
            resultat.pointsDePassage.add(
                    new PointDePassage(stations.get(i).getStation(), ligne)
            );
        }

        resultat.nombreStations = resultat.pointsDePassage.size();
        return resultat;
    }

    private int trouverPosition(List<LigneStation> stations, Station station) {
        for (int i = 0; i < stations.size(); i++) {
            if (stations.get(i).getStation().getId().equals(station.getId())) {
                return i;
            }
        }
        return -1;
    }

    private ItineraireResult chercherAvecUnChangement(Station depart, Station arrivee, Ligne ligneDepart) {

        List<LigneStation> stationsDepart = ligneStationRepository.findByLigneOrderByOrdre(ligneDepart);

        for (LigneStation ls : stationsDepart) {
            Station correspondance = ls.getStation();

            for (LigneStation autreLS : ligneStationRepository.findByStation(correspondance)) {
                Ligne autreLigne = autreLS.getLigne();

                if (autreLigne.getId().equals(ligneDepart.getId())) continue;

                ItineraireResult secondePartie =
                        chercherTrajetDirect(correspondance, arrivee, autreLigne);

                if (secondePartie != null) {
                    ItineraireResult premierePartie =
                            chercherTrajetDirect(depart, correspondance, ligneDepart);

                    if (premierePartie != null) {
                        ItineraireResult resultat = new ItineraireResult();
                        resultat.stationDepart = depart;
                        resultat.stationArrivee = arrivee;
                        resultat.pointsDePassage = new ArrayList<>();

                        resultat.pointsDePassage.addAll(premierePartie.pointsDePassage);
                        resultat.pointsDePassage.addAll(secondePartie.pointsDePassage);

                        resultat.nombreChangements = 1;
                        resultat.nombreStations = resultat.pointsDePassage.size();
                        return resultat;
                    }
                }
            }
        }
        return null;
    }
}