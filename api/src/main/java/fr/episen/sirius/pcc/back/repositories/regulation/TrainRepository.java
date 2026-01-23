package fr.episen.sirius.pcc.back.repositories.regulation;

import fr.episen.sirius.pcc.back.models.regulation.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainRepository extends JpaRepository<Train, Long> {
}
