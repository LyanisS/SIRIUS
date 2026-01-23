package fr.episen.sirius.pcc.back.repositories.regulation;

import fr.episen.sirius.pcc.back.models.regulation.ElementVoie;
import fr.episen.sirius.pcc.back.models.regulation.LigneStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ElementVoieRepository extends JpaRepository<ElementVoie, Long> {
    @Query(value="SELECT e FROM ElementVoie e " +
            "WHERE e.id IN (SELECT e2.elementSuivant FROM ElementVoie e2 WHERE e2.ligneStation = :previousLigneStation) " +
            "AND e.elementSuivant IN (SELECT e3.id FROM ElementVoie e3 WHERE e3.ligneStation = :nextLigneStation)")
    ElementVoie findElementVoieBetweenStations(@Param("previousLigneStation") LigneStation previousLigneStation, @Param("nextLigneStation") LigneStation nextLigneStation);
}
