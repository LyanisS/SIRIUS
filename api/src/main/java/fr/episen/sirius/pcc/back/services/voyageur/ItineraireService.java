package fr.episen.sirius.pcc.back.services.voyageur;

import fr.episen.sirius.pcc.back.models.regulation.*;
import fr.episen.sirius.pcc.back.models.voyageur.ItineraireResult;
import fr.episen.sirius.pcc.back.models.voyageur.PointDePassage;
import fr.episen.sirius.pcc.back.repositories.regulation.*;
import fr.episen.sirius.pcc.back.services.voyageur.graph.Voisin;
import fr.episen.sirius.pcc.back.services.voyageur.graph.StationDistance;
import fr.episen.sirius.pcc.back.services.voyageur.graph.DijkstraResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ItineraireService {

    @Autowired private StationRepository stationRepository;
    @Autowired private LigneRepository ligneRepository;
    @Autowired private LigneStationRepository ligneStationRepository;
    @Autowired private ElementVoieRepository elementVoieRepository;

    // calcul itineraire entre 2 stations
    public ItineraireResult calculerItineraire(Long departId, Long arriveeId) {

        Station depart  = stationRepository.findById(departId).orElse(null);
        Station arrivee = stationRepository.findById(arriveeId).orElse(null);

        if (depart == null || arrivee == null) return null;

        //creation graphe metro
        Map<Long, List<Voisin>> graphe = construireGraphe();
        DijkstraResult dijkstra = dijkstra(graphe, departId, arriveeId);
        List<Long> chemin = reconstruireChemin(dijkstra.precedent, departId, arriveeId);

        //résultat
        ItineraireResult resultat = new ItineraireResult();
        resultat.stationDepart   = depart;
        resultat.stationArrivee  = arrivee;
        resultat.pointsDePassage = new ArrayList<>();

        int changements      = 0;
        Long lignePrecedente = null;

        for (int i = 0; i < chemin.size(); i++) {
            Station stationCourante = stationRepository.findById(chemin.get(i)).orElse(null);
            Ligne ligneCourante = null;

            if (i < chemin.size() - 1) {
                Station stationSuivante = stationRepository.findById(chemin.get(i + 1)).orElse(null);
                ligneCourante = trouverLigneEntreStations(stationCourante, stationSuivante);

                // si on change la ligne
                if (lignePrecedente != null && ligneCourante != null
                        && !lignePrecedente.equals(ligneCourante.getId())) {
                    changements++;
                }
                if (ligneCourante != null) {
                    lignePrecedente = ligneCourante.getId();
                } else {
                    lignePrecedente = null;
                }
            }

            resultat.pointsDePassage.add(new PointDePassage(stationCourante, ligneCourante));
        }

        resultat.nombreStations    = chemin.size();
        resultat.nombreChangements = changements;

        return resultat;
    }
    // algo dijkstra
    private DijkstraResult dijkstra(Map<Long, List<Voisin>> graphe, Long depart, Long arrivee) {

        Map<Long, Integer> distances = new HashMap<>();
        Map<Long, Long>    precedent = new HashMap<>();
        PriorityQueue<StationDistance> file = new PriorityQueue<>();

        // On initialise toutes les distances à l'infini
        for (Long station : graphe.keySet()) {
            distances.put(station, Integer.MAX_VALUE);
        }
        distances.put(depart, 0);
        file.add(new StationDistance(depart, 0));

        while (!file.isEmpty()) {
            StationDistance courant = file.poll();

            if (courant.stationId.equals(arrivee)) break;

            if (courant.distance > distances.get(courant.stationId)) continue;

            for (Voisin voisin : graphe.getOrDefault(courant.stationId, new ArrayList<>())) {
                int nouvelleDistance = distances.get(courant.stationId) + voisin.distance;

                if (nouvelleDistance < distances.getOrDefault(voisin.stationId, Integer.MAX_VALUE)) {
                    distances.put(voisin.stationId, nouvelleDistance);
                    precedent.put(voisin.stationId, courant.stationId);
                    file.add(new StationDistance(voisin.stationId, nouvelleDistance));
                }
            }
        }

        return new DijkstraResult(distances, precedent);
    }

    // creation du graphe du reseau
    private Map<Long, List<Voisin>> construireGraphe() {

        Map<Long, List<Voisin>> graphe = new HashMap<>();

        for (Ligne ligne : ligneRepository.findAll()) {
            List<LigneStation> stations = ligneStationRepository.findByLigneOrderByOrdre(ligne);

            for (int i = 0; i < stations.size() - 1; i++) {
                LigneStation a = stations.get(i);
                LigneStation b = stations.get(i + 1);
                int distance   = distanceEntreStations(a, b);

                Long idA = a.getStation().getId();
                Long idB = b.getStation().getId();

                // on relie les deux stations dans les deux sens
                graphe.computeIfAbsent(idA, k -> new ArrayList<>()).add(new Voisin(idB, distance));
                graphe.computeIfAbsent(idB, k -> new ArrayList<>()).add(new Voisin(idA, distance));
            }
        }

        return graphe;
    }
    // calcul de distance entre deux stations(j'ai utilisé elementVoie)
    private int distanceEntreStations(LigneStation from, LigneStation to) {
        ElementVoie ev = elementVoieRepository.findFirstByLigneStationId(from.getId()).orElse(null);
        int total = 0;

        while (ev != null) {
            total += (ev.getLongueur() != null ? ev.getLongueur() : 0);
            if (ev.getLigneStation() != null && ev.getLigneStation().getId().equals(to.getId())) break;
            ev = ev.getElementSuivant();
        }

        return Math.max(total, 1); // distance minimum de 1 pour éviter les zéros
    }

    private List<Long> reconstruireChemin(Map<Long, Long> precedent, Long depart, Long arrivee) {
        List<Long> chemin = new ArrayList<>();
        Long actuel = arrivee;

        while (actuel != null) {
            chemin.add(actuel);
            actuel = precedent.get(actuel);
        }

        Collections.reverse(chemin);
        return chemin;
    }

    // Trouve la ligne commune entre deux stations
    private Ligne trouverLigneEntreStations(Station a, Station b) {
        List<LigneStation> lignesA = ligneStationRepository.findByStation(a);
        List<LigneStation> lignesB = ligneStationRepository.findByStation(b);

        for (LigneStation la : lignesA) {
            for (LigneStation lb : lignesB) {
                boolean memeLigne    = la.getLigne().getId().equals(lb.getLigne().getId());
                boolean stationsVoisines = Math.abs(la.getOrdre() - lb.getOrdre()) == 1;

                if (memeLigne && stationsVoisines) {
                    return la.getLigne();
                }
            }
        }

        return null;
    }
}