package fr.episen.sirius.pcc.back.repositories.regulation;

import fr.episen.sirius.pcc.back.models.regulation.ElementVoie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ElementVoieRepository extends JpaRepository<ElementVoie, Long> {

    // Trouve l'ElementVoie correspondant à une LigneStation
    Optional<ElementVoie> findByLigneStationId(Long ligneStationId);
}
