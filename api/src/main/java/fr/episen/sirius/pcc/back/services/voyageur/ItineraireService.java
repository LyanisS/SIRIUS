package fr.episen.sirius.pcc.back.services.voyageur;

import fr.episen.sirius.pcc.back.dto.voyageur.CreateItineraireFavoriDTO;
import fr.episen.sirius.pcc.back.dto.voyageur.EtapeItineraireDTO;
import fr.episen.sirius.pcc.back.dto.voyageur.ItineraireDTO;
import fr.episen.sirius.pcc.back.dto.voyageur.ItineraireFavoriDTO;
import fr.episen.sirius.pcc.back.models.regulation.Ligne;
import fr.episen.sirius.pcc.back.models.regulation.LigneStation;
import fr.episen.sirius.pcc.back.models.regulation.Station;
import fr.episen.sirius.pcc.back.models.voyageur.Itineraire;
import fr.episen.sirius.pcc.back.models.voyageur.Utilisateur;
import fr.episen.sirius.pcc.back.repositories.regulation.LigneStationRepository;
import fr.episen.sirius.pcc.back.repositories.regulation.StationRepository;
import fr.episen.sirius.pcc.back.repositories.voyageur.ItineraireRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ItineraireService {

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private LigneStationRepository ligneStationRepository;

    @Autowired
    private ItineraireRepository itineraireRepository;

    public ItineraireDTO calculerItineraire(Long stationDepartId, Long stationArriveeId) {

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

        ItineraireDTO meilleur = null;

        for (LigneStation ls : lignesDepart) {
            Ligne ligne = ls.getLigne();

            ItineraireDTO direct = chercherTrajetDirect(depart, arrivee, ligne);
            if (direct != null && (meilleur == null || direct.nombreStations < meilleur.nombreStations)) {
                meilleur = direct;
            }

            ItineraireDTO avecChangement = chercherAvecUnChangement(depart, arrivee, ligne);
            if (avecChangement != null && (meilleur == null || avecChangement.nombreStations < meilleur.nombreStations)) {
                meilleur = avecChangement;
            }
        }

        return meilleur;
    }

    private ItineraireDTO chercherTrajetDirect(Station depart, Station arrivee, Ligne ligne) {

        List<LigneStation> stations = ligneStationRepository.findByLigneOrderByOrdre(ligne);

        int indexDepart = trouverPosition(stations, depart);
        int indexArrivee = trouverPosition(stations, arrivee);

        if (indexDepart == -1 || indexArrivee == -1) {
            return null;
        }

        ItineraireDTO resultat = new ItineraireDTO();
        resultat.stationDepart = depart;
        resultat.stationArrivee = arrivee;
        resultat.etapes = new ArrayList<>();
        resultat.nombreChangements = 0;

        int debut = Math.min(indexDepart, indexArrivee);
        int fin = Math.max(indexDepart, indexArrivee);

        for (int i = debut; i <= fin; i++) {
            resultat.etapes.add(
                    new EtapeItineraireDTO(stations.get(i).getStation(), ligne)
            );
        }

        resultat.nombreStations = resultat.etapes.size();
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

    private ItineraireDTO chercherAvecUnChangement(Station depart, Station arrivee, Ligne ligneDepart) {

        List<LigneStation> stationsDepart = ligneStationRepository.findByLigneOrderByOrdre(ligneDepart);

        for (LigneStation ls : stationsDepart) {
            Station correspondance = ls.getStation();

            for (LigneStation autreLS : ligneStationRepository.findByStation(correspondance)) {
                Ligne autreLigne = autreLS.getLigne();

                if (autreLigne.getId().equals(ligneDepart.getId())) continue;

                ItineraireDTO secondePartie =
                        chercherTrajetDirect(correspondance, arrivee, autreLigne);

                if (secondePartie != null) {
                    ItineraireDTO premierePartie =
                            chercherTrajetDirect(depart, correspondance, ligneDepart);

                    if (premierePartie != null) {
                        ItineraireDTO resultat = new ItineraireDTO();
                        resultat.stationDepart = depart;
                        resultat.stationArrivee = arrivee;
                        resultat.etapes = new ArrayList<>();

                        resultat.etapes.addAll(premierePartie.etapes);
                        resultat.etapes.addAll(secondePartie.etapes);

                        resultat.nombreChangements = 1;
                        resultat.nombreStations = resultat.etapes.size();
                        return resultat;
                    }
                }
            }
        }
        return null;
    }

    public List<ItineraireFavoriDTO> getAllItinerairesFavoris(Utilisateur utilisateur) {
        List<ItineraireFavoriDTO> itineraireFavorisDTO = new ArrayList<>();

        for (Itineraire itineraireFavori : itineraireRepository.findAllByUtilisateurOrderByIdDesc(utilisateur)) {
            itineraireFavorisDTO.add(new ItineraireFavoriDTO(itineraireFavori));
        }

        return itineraireFavorisDTO;
    }

    public Optional<ItineraireFavoriDTO> createItineraireFavori(Utilisateur utilisateur, CreateItineraireFavoriDTO dto) {
        Optional<Station> stationDepart = stationRepository.findById(dto.getStationDepartId());
        if (stationDepart.isEmpty()) return Optional.empty();

        Optional<Station> stationArrivee = stationRepository.findById(dto.getStationArriveeId());
        if (stationArrivee.isEmpty()) return Optional.empty();

        Itineraire itineraire = new Itineraire();
        itineraire.setUtilisateur(utilisateur);
        if (dto.getDate() != null) {
            itineraire.setDate(dto.getDate());
        } else {
            itineraire.setDate(new Date());
        }
        itineraire.setDepart(dto.isDepart());
        itineraire.setStationDepart(stationDepart.get());
        itineraire.setStationArrivee(stationArrivee.get());

        return Optional.of(new ItineraireFavoriDTO(itineraireRepository.save(itineraire)));
    }
}