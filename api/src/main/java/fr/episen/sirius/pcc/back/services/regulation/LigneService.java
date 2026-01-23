package fr.episen.sirius.pcc.back.services.regulation;

import fr.episen.sirius.pcc.back.models.regulation.Ligne;
import fr.episen.sirius.pcc.back.repositories.regulation.LigneRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class LigneService {
    @Autowired
    private LigneRepository ligneRepository;

    /**
     * Récupère la liste de toutes les lignes
     * @return Liste des lignes
     */
    public List<Ligne> getAllLignes() {
        log.info("Récupération de toutes les lignes");
        List<Ligne> lignes = ligneRepository.findAll();
        log.info("Nombre de lignes trouvées: {}", lignes.size());
        return lignes;
    }
}