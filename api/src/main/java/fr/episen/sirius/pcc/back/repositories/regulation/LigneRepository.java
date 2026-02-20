package fr.episen.sirius.pcc.back.repositories.regulation;

import fr.episen.sirius.pcc.back.models.regulation.Ligne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LigneRepository extends JpaRepository<Ligne, Long> {
}
