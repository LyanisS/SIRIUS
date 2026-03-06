package fr.episen.sirius.pcc.back.services.voyageur;

import fr.episen.sirius.pcc.back.dto.voyageur.SessionUtilisateurDTO;
import fr.episen.sirius.pcc.back.dto.voyageur.UtilisateurDTO;
import fr.episen.sirius.pcc.back.models.voyageur.Utilisateur;
import fr.episen.sirius.pcc.back.repositories.voyageur.UtilisateurRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;

@Service
@Slf4j
public class UtilisateurService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public SessionUtilisateurDTO inscription(String nom, String email, String motDePasse) {
        if (!email.contains("@")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Le format de l'email est invalide.");
        }

        if (utilisateurRepository.existsByEmail(email.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un compte existe déjà avec cet email.");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(nom);
        utilisateur.setEmail(email.toLowerCase());
        utilisateur.setMotDePasse(passwordEncoder.encode(motDePasse));

        utilisateur = utilisateurRepository.save(utilisateur);

        return buildSession(utilisateur);
    }

    public SessionUtilisateurDTO connexion(String email, String motDePasse) {
        if (!email.contains("@")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Le format de l'email est invalide.");
        }

        Optional<Utilisateur> utilisateur = utilisateurRepository.findByEmail(email.toLowerCase());
        if (utilisateur.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect.");
        }

        if (!passwordEncoder.matches(motDePasse, utilisateur.get().getMotDePasse())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect.");
        }

        return buildSession(utilisateur.get());
    }

    private SessionUtilisateurDTO buildSession(Utilisateur utilisateur) {
        String token = jwtService.generateToken(utilisateur);

        UtilisateurDTO utilisateurDTO = new UtilisateurDTO();
        utilisateurDTO.setId(utilisateur.getId());
        utilisateurDTO.setNom(utilisateur.getNom());
        utilisateurDTO.setEmail(utilisateur.getEmail());

        SessionUtilisateurDTO session = new SessionUtilisateurDTO();
        session.setToken(token);
        session.setUtilisateur(utilisateurDTO);

        return session;
    }
}