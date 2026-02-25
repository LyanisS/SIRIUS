package fr.episen.sirius.pcc.back.dto.voyageur;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ConnexionUtilisateurDTO {
    @NotBlank(message = "email is required")
    private String email;

    @NotBlank(message = "motDePasse is required")
    private String motDePasse;
}
