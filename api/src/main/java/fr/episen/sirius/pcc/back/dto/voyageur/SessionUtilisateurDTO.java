package fr.episen.sirius.pcc.back.dto.voyageur;

import lombok.Data;

@Data
public class SessionUtilisateurDTO {
    private String token;

    private UtilisateurDTO utilisateur;
}
