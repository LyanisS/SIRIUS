package fr.episen.sirius.pcc.back.controllers.voyageur;

import fr.episen.sirius.pcc.back.dto.voyageur.CreateUtilisateurDTO;
import fr.episen.sirius.pcc.back.dto.voyageur.ConnexionUtilisateurDTO;
import fr.episen.sirius.pcc.back.dto.voyageur.SessionUtilisateurDTO;
import fr.episen.sirius.pcc.back.services.voyageur.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("utilisateurs")
@CrossOrigin(origins = "*")
public class UtilisateurController {

    @Autowired
    private UtilisateurService utilisateurService;

    @PostMapping("/inscription")
    public ResponseEntity<SessionUtilisateurDTO> inscription(@Valid @RequestBody CreateUtilisateurDTO dto) {

        return ResponseEntity.ok(utilisateurService.inscription(dto.getNom(), dto.getEmail(), dto.getMotDePasse()));
    }

    @PostMapping("/connexion")
    public ResponseEntity<SessionUtilisateurDTO> connexion(@Valid @RequestBody ConnexionUtilisateurDTO dto) {
        return ResponseEntity.ok(utilisateurService.connexion(dto.getEmail(), dto.getMotDePasse()));
    }
}
