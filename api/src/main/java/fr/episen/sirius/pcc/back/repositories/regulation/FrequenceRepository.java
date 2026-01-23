package fr.episen.sirius.pcc.back.repositories.regulation;

import fr.episen.sirius.pcc.back.models.regulation.Frequence;
import fr.episen.sirius.pcc.back.models.regulation.Ligne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.sql.Time;
import java.util.Date;
import java.util.List;

@Repository
public interface FrequenceRepository extends JpaRepository<Frequence, Long> {

    /**
     * Trouve les fréquences applicables pour une ligne, un jour et une heure donnés
     */
    @Query("SELECT f FROM Frequence f " +
            "JOIN f.jours j " +
            "WHERE f.ligne = :ligne " +
            "AND j.id = :jourId " +
            "AND f.dateDebut <= :date " +
            "AND f.dateFin >= :date " +
            "AND f.heureDebut <= :heure " +
            "AND f.heureFin >= :heure")
    List<Frequence> findFrequenceApplicable(
            @Param("ligne") Ligne ligne,
            @Param("jourId") Long jourId,
            @Param("date") Date date,
            @Param("heure") Time heure
    );
}
    @Query(value = "SELECT * FROM Frequence " +
            "WHERE datedebut <= CURRENT_DATE " +
            "AND datefin >= CURRENT_DATE " +
            "AND (heuredebut <= CURRENT_TIME " +
            "OR heuredebut <= CURRENT_TIME + INTERVAL '?1 hours')",
            nativeQuery = true)
    List<Frequence> findActiveFrequencesForHours(@Param("hours") int hours);
}
