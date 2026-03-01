package fr.episen.sirius.pcc.back.repositories.voyageur;

import fr.episen.sirius.pcc.back.models.voyageur.Itineraire;
import fr.episen.sirius.pcc.back.models.voyageur.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItineraireRepository extends JpaRepository<Itineraire, Long> {
    List<Itineraire> findAllByUtilisateurOrderByIdDesc(Utilisateur utilisateur);
}
