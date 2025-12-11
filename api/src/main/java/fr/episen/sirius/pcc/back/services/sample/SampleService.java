package fr.episen.sirius.pcc.back.services.sample;

import fr.episen.sirius.pcc.back.models.sample.Sample;
import fr.episen.sirius.pcc.back.repositories.sample.SampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SampleService {

    @Autowired
    private SampleRepository sampleRepository;

    public Sample findOldestSample(){
        return sampleRepository.findLastSampleByDate();
    }

    public Sample findByIdSample(Long idSample) {
        Optional<Sample> optionalSample = sampleRepository.findById(idSample);
        return optionalSample.orElse(null);
    }

    public List<Sample> findAllSample(){
        return sampleRepository.findAll();
    }

    public boolean updateSampleDate(Sample updatedSample) {
        Optional<Sample> optionalSample = sampleRepository.findById(updatedSample.getIdSample());
        if(optionalSample.isPresent()){
            Sample sample = optionalSample.get();
            sample.setDateSample(updatedSample.getDateSample());
            sampleRepository.saveAndFlush(sample);
            return true;
        }
        return false;
    }

    public boolean deleteSample(Long idSample) {
        Optional<Sample> optionalSample = sampleRepository.findById(idSample);
        if (optionalSample.isPresent()) {
            optionalSample.ifPresent(sample -> sampleRepository.delete(sample));
            return true;
        }
        return false;
    }
}