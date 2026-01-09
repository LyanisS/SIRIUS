package fr.episen.sirius.pcc.back.repositories.regulation;

import fr.episen.sirius.pcc.back.models.regulation.Frequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FrequenceRepository extends JpaRepository<Frequence, Long> {
    @Query(value="SELECT * FROM Frequence WHERE datedebut <= CURRENT_DATE + ?1 AND datefin >= CURRENT_DATE", nativeQuery = true)
    List<Frequence> findActiveFrequencesForDays(int days);
}
