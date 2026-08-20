package fr.episen.sirius.pcc.back.dto.voyageur;

import lombok.Data;

@Data
public class IncidentImpactDTO {
    private Long id;
    private String message;
    private String ligneNom;
    private int dureeImpactMinutes;
    private boolean enCours;
}
