package fr.episen.sirius.pcc.back.repositories.regulation;

import fr.episen.sirius.pcc.back.models.regulation.Ligne;
import fr.episen.sirius.pcc.back.models.regulation.LigneStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LigneStationRepository extends JpaRepository<LigneStation, Long> {
    List<LigneStation> findByLigneOrderByOrdreAsc(Ligne ligne);

    List<LigneStation> findByLigneOrderByOrdreDesc(Ligne ligne);
}
