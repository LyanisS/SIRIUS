package fr.episen.sirius.pcc.back.repositories.regulation;

import fr.episen.sirius.pcc.back.models.regulation.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    @Query("SELECT i FROM Incident i " +
            "WHERE DATE(i.dateDebut) = CURRENT_DATE " +
            "OR (i.dateFin IS NOT NULL AND DATE(i.dateFin) = CURRENT_DATE)")
    List<Incident> findTodayIncidents();

    @Query("SELECT i FROM Incident i " +
            "WHERE i.trajet.ligne.id IN :ligneIds " +
            "AND i.dateDebut <= :fenetreFin " +
            "AND (i.dateFin IS NULL OR i.dateFin >= :fenetreDebut)")
    List<Incident> findIncidentsActifsSurLignes(
            @Param("ligneIds") Set<Long> ligneIds,
            @Param("fenetreDebut") Date fenetreDebut,
            @Param("fenetreFin") Date fenetreFin);
}
