package fr.episen.sirius.pcc.back.repositories.regulation;

import fr.episen.sirius.pcc.back.models.regulation.Frequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FrequenceRepository extends JpaRepository<Frequence, Long> {
    @Query(value = "SELECT * FROM Frequence " +
            "WHERE datedebut <= CURRENT_DATE " +
            "AND datefin >= CURRENT_DATE " +
            "AND (heuredebut <= CURRENT_TIME " +
            "OR heuredebut <= CURRENT_TIME + INTERVAL '?1 hours')",
            nativeQuery = true)
    List<Frequence> findActiveFrequencesForHours(@Param("hours") int hours);
}
