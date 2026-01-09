package fr.episen.sirius.pcc.back.repositories.regulation;

import fr.episen.sirius.pcc.back.models.regulation.Trajet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrajetRepository extends JpaRepository<Trajet, Long> {
    @Query(value="SELECT * FROM Trajet", nativeQuery = true)
    List<Trajet> findAllTrajets();
}
