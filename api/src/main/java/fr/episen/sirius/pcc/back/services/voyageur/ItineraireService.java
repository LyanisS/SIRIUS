package fr.episen.sirius.pcc.back.services.voyageur;

import fr.episen.sirius.pcc.back.models.regulation.Ligne;
import fr.episen.sirius.pcc.back.models.regulation.LigneStation;
import fr.episen.sirius.pcc.back.models.regulation.Station;
import fr.episen.sirius.pcc.back.repositories.regulation.LigneStationRepository;
import fr.episen.sirius.pcc.back.repositories.regulation.StationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class ItineraireService {
    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private LigneStationRepository ligneStationRepository;

    public ItineraireResult calculerItineraire(Long stationDepartId, Long stationArriveeId) {
        // Récupérer les stations de départ et d'arrivée
        Station depart = stationRepository.findById(stationDepartId).orElse(null);
        Station arrivee = stationRepository.findById(stationArriveeId).orElse(null);

        if (depart == null || arrivee == null) {
            log.error("Station introuvable");
            return null;
        }

        log.info("Recherche d'itinéraire | Départ : {} | Arrivée : {}", depart.getNom(), arrivee.getNom());

        // Les lignes qui passent par station de départ
        List<LigneStation> lignesDepart = ligneStationRepository.findByStation(depart);

        if (lignesDepart.isEmpty()) {
            log.error("Aucune ligne ne dessert la station {}", depart.getNom());
            return null;
        }

        ItineraireResult meilleurItineraire = null;

        for (LigneStation ligneDepart : lignesDepart) {
            Ligne ligne = ligneDepart.getLigne();

            // un trajet sans correspondance
            ItineraireResult direct = chercherTrajetDirect(depart, arrivee, ligne);
            if (direct != null) {
                if (meilleurItineraire == null || direct.nombreStations < meilleurItineraire.nombreStations) {
                    meilleurItineraire = direct;
                }
            }

            // trajet avec un correspondance
            ItineraireResult avecChangement = chercherAvecUnChangement(depart, arrivee, ligne);
            if (avecChangement != null) {
                if (meilleurItineraire == null || avecChangement.nombreStations < meilleurItineraire.nombreStations) {
                    meilleurItineraire = avecChangement;
                }
            }
        }

        if (meilleurItineraire != null) {
            afficherItineraire(meilleurItineraire);
        } else {
            log.warn("Aucun itinéraire trouvé");
        }

        return meilleurItineraire;
    }

    /**les logs*/
    private void afficherItineraire(ItineraireResult resultat) {
        log.info(" Trajet trouvé ({} stations, {} changement(s))",
                resultat.nombreStations, resultat.nombreChangements);

        String ligneActuelle = null;
        List<String> stationsLigneActuelle = new ArrayList<>();

        for (int i = 0; i < resultat.pointsDePassage.size(); i++) {
            PointDePassage point = resultat.pointsDePassage.get(i);
            String nomLigne = point.ligne.getNom();
            String nomStation = point.station.getNom();

            if (ligneActuelle != null && !nomLigne.equals(ligneActuelle)) {
                log.info("   {} : {}", ligneActuelle, String.join(" → ", stationsLigneActuelle));
                log.info("    Correspondance");
                stationsLigneActuelle.clear();
            }

            if (ligneActuelle == null || !nomLigne.equals(ligneActuelle)) {
                ligneActuelle = nomLigne;
            }

            stationsLigneActuelle.add(nomStation);
        }

        if (!stationsLigneActuelle.isEmpty()) {
            log.info("   {} : {}", ligneActuelle, String.join(" → ", stationsLigneActuelle));
        }
    }

    private ItineraireResult chercherTrajetDirect(Station depart, Station arrivee, Ligne ligne) {
        // Récupérer toutes les stations dans l'ordre
        List<LigneStation> stationsDeLaLigne = ligneStationRepository.findByLigneOrderByOrdre(ligne);

        int indexDepart = trouverPosition(stationsDeLaLigne, depart);
        int indexArrivee = trouverPosition(stationsDeLaLigne, arrivee);

        // Si les deux stations ne sont pas sur la ligne = pas de trajet
        if (indexDepart == -1 || indexArrivee == -1) {
            return null;
        }

        ItineraireResult resultat = new ItineraireResult();
        resultat.stationDepart = depart;
        resultat.stationArrivee = arrivee;
        resultat.pointsDePassage = new ArrayList<>();
        resultat.nombreChangements = 0;

        // les stations desservies entre départ et arrivée
        int debut = Math.min(indexDepart, indexArrivee);
        int fin = Math.max(indexDepart, indexArrivee);

        for (int i = debut; i <= fin; i++) {
            Station station = stationsDeLaLigne.get(i).getStation();
            resultat.pointsDePassage.add(new PointDePassage(station, ligne));
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

    //Trajet avec changement de ligne
    private ItineraireResult chercherAvecUnChangement(Station depart, Station arrivee, Ligne ligneDepart) {
        // Récupérer stations de la ligne de départ
        List<LigneStation> stationsLigneDepart = ligneStationRepository.findByLigneOrderByOrdre(ligneDepart);

        for (LigneStation ls : stationsLigneDepart) {
            Station stationCorrespondance = ls.getStation();

            // Les autres lignes qui passent par cette station
            List<LigneStation> autresLignes = ligneStationRepository.findByStation(stationCorrespondance);

            for (LigneStation autreLS : autresLignes) {
                Ligne autreLigne = autreLS.getLigne();

                if (autreLigne.getId().equals(ligneDepart.getId())) {
                    continue;
                }

                // Vérifier si on peut arrivée à destination depuis cette station (de correspondance)
                ItineraireResult deuxiemePartie = chercherTrajetDirect(stationCorrespondance, arrivee, autreLigne);

                if (deuxiemePartie != null) {
                    ItineraireResult premierePartie = chercherTrajetDirect(depart, stationCorrespondance, ligneDepart);

                    if (premierePartie != null) {
                        ItineraireResult resultat = new ItineraireResult();
                        resultat.stationDepart = depart;
                        resultat.stationArrivee = arrivee;
                        resultat.pointsDePassage = new ArrayList<>();

                        resultat.pointsDePassage.addAll(premierePartie.pointsDePassage);

                        for (int i = 0; i < deuxiemePartie.pointsDePassage.size(); i++) {
                            resultat.pointsDePassage.add(deuxiemePartie.pointsDePassage.get(i));
                        }

                        resultat.nombreChangements = 1;
                        resultat.nombreStations = resultat.pointsDePassage.size();

                        return resultat;
                    }
                }
            }
        }

        return null;
    }

    public static class PointDePassage {
        public Station station;
        public Ligne ligne;

        public PointDePassage(Station station, Ligne ligne) {
            this.station = station;
            this.ligne = ligne;
        }
    }

    public static class ItineraireResult {
        public Station stationDepart;
        public Station stationArrivee;
        public List<PointDePassage> pointsDePassage;
        public int nombreChangements;
        public int nombreStations;
    }
}