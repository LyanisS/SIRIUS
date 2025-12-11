package fr.episen.sirius.pcc.back.repositories.sample;

import fr.episen.sirius.pcc.back.models.sample.Sample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SampleRepository extends JpaRepository<Sample, Long> {
    @Query(value="SELECT * FROM Sample AS s ORDER BY s.date_sample DESC LIMIT 1", nativeQuery = true)
    Sample findLastSampleByDate();
}