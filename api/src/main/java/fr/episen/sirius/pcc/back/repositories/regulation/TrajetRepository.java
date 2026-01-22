package fr.episen.sirius.pcc.back.repositories.regulation;

import fr.episen.sirius.pcc.back.models.regulation.Trajet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrajetRepository extends JpaRepository<Trajet, Long> {
    @Query("SELECT DISTINCT t FROM Trajet t " +
            "JOIN Horaire h ON h.trajet = t " +
            "WHERE (SELECT MIN(h2.dateDepartTheorique) FROM Horaire h2 WHERE h2.trajet = t) <= NOW() " +
            "AND (SELECT MAX(h3.dateArriveeTheorique) FROM Horaire h3 WHERE h3.trajet = t) >= NOW()")
    List<Trajet> findActiveTrajets();
}
