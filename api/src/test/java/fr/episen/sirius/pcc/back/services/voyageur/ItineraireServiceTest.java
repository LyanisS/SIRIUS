package fr.episen.sirius.pcc.back.services.voyageur;

import fr.episen.sirius.pcc.back.services.voyageur.graph.DijkstraResult;
import fr.episen.sirius.pcc.back.services.voyageur.graph.Voisin;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItineraireServiceTest {

    @Test
    void doitAvoirAucunRetardSansIncident() {

        // Given : aucun incident sur l'itinéraire
        ItineraireService service = new ItineraireService();
        List<IncidentImpactDTO> incidents = new ArrayList<>();

        // When : on calcule le retard
        int retard = service.calculerRetardTotal(incidents);

        // Then : sans incident il ne doit pas y avoir de retard
        assertEquals(0, retard);
    }