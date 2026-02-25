package fr.episen.sirius.pcc.back.dto.voyageur;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CreateUtilisateurDTO {
    @NotBlank(message = "nom is required")
    private String nom;

    @NotBlank(message = "email is required")
    private String email;

    @NotBlank(message = "motDePasse ID is required")
    private String motDePasse;
}
