package fr.episen.sirius.pcc.back.dto.voyageur;

import fr.episen.sirius.pcc.back.models.voyageur.Utilisateur;import lombok.Data;

@Data
public class UtilisateurDTO {
    private Long id;

    private String nom;

    private String email;

    public UtilisateurDTO() {}

    public UtilisateurDTO(Utilisateur utilisateur) {
        this.id = utilisateur.getId();
        this.nom = utilisateur.getNom();
        this.email = utilisateur.getEmail();
    }
}
