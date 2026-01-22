package fr.episen.sirius.pcc.back.repositories.regulation;

import fr.episen.sirius.pcc.back.models.regulation.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    @Query("SELECT i FROM Incident i " +
            "WHERE DATE(i.dateDebut) = CURRENT_DATE " +
            "OR (i.dateFin IS NOT NULL AND DATE(i.dateFin) = CURRENT_DATE)")
    List<Incident> findTodayIncidents();
}
