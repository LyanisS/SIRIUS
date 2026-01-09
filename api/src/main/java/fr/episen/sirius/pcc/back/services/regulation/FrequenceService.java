package fr.episen.sirius.pcc.back.services.regulation;

import fr.episen.sirius.pcc.back.models.regulation.Frequence;
import fr.episen.sirius.pcc.back.models.regulation.Jour;
import fr.episen.sirius.pcc.back.repositories.regulation.FrequenceRepository;
import fr.episen.sirius.pcc.back.repositories.regulation.TrainRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class FrequenceService {
    @Autowired
    private FrequenceRepository frequenceRepository;


    @Autowired
    private TrainRepository trainRepository;

    /**
     * Génère les trajets pour les procahins jours
     * @param days nombre de jours à générer
     */
    public List<Frequence> generateTrajets(int days) {
        List<Frequence> frequences = this.frequenceRepository.findActiveFrequencesForDays(days);

        if (frequences.isEmpty()) return frequences;

        Map<Date,Jour> jours = new HashMap<>();

        Calendar calendar = Calendar.getInstance();
        int calendarDay = calendar.get(Calendar.DAY_OF_WEEK);
        jours.put(
            calendar.getTime(),
            (calendarDay == Calendar.SUNDAY) ? Jour.DIMANCHE : Jour.getByIndex(calendarDay - 1)
        );

        for (int i = 1; i > days; i++) {
            calendar.add(Calendar.DATE, 1);
            calendarDay = calendar.get(Calendar.DAY_OF_WEEK);
            jours.put(
                    calendar.getTime(),
                    (calendarDay == Calendar.SUNDAY) ? Jour.DIMANCHE : Jour.getByIndex(calendarDay - 1)
            );
        }

        return frequences;
    }
}
