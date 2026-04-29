package fr.episen.sirius.pcc.back.services.regulation;

import fr.episen.sirius.pcc.back.models.regulation.ElementVoie;
import fr.episen.sirius.pcc.back.models.regulation.Train;
import fr.episen.sirius.pcc.back.repositories.regulation.TrainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrainServiceTest {

    private TrainRepository trainRepository;

    @BeforeEach
    public void setUp() {
        trainRepository = Mockito.mock(TrainRepository.class);
    }

    @Test
    public void testUnTrainParElementVoie() {

        // Given : deux trains sur deux EV differents
        ElementVoie ev1 = new ElementVoie();
        ev1.setId(1L);

        ElementVoie ev2 = new ElementVoie();
        ev2.setId(2L);

        Train train1 = new Train();
        train1.setId(1L);
        train1.setPosition(ev1);

        Train train2 = new Train();
        train2.setId(2L);
        train2.setPosition(ev2);

        List<Train> trains = new ArrayList<>();
        trains.add(train1);
        trains.add(train2);

        Mockito.when(trainRepository.findAll()).thenReturn(trains);

        // When : on verifie la règle
        List<Train> tousLesTrains = trainRepository.findAll();
        Set<Long> evDejaOccupes = new HashSet<>();
        boolean regleRespectee = true;

        for (Train train : tousLesTrains) {
            if (train.getPosition() == null) continue;
            Long evId = train.getPosition().getId();
            if (evDejaOccupes.contains(evId)) {
                regleRespectee = false;
                break;
            }
            evDejaOccupes.add(evId);
        }

        // Then : la regle doit etre respectèe
        assertTrue(regleRespectee, "Chaque train doit etre sur un EV diffèrent");
    }
}