package fr.episen.sirius.pcc.back.repositories.sample;

import fr.episen.sirius.pcc.back.models.sample.Sample;
import fr.episen.sirius.pcc.back.models.sample.SampleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SampleRepository extends JpaRepository<Sample, Long> {
    @Query(value="SELECT * FROM Sample AS s ORDER BY s.date_sample DESC LIMIT 1", nativeQuery = true)
    Sample findLastSampleByDate();

    @Query("SELECT s FROM Sample AS s WHERE s.sampleType = :sampleType")
    List<Sample> findSampleType(@Param("sampleType") SampleType sampleType);
}