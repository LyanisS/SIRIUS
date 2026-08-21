package fr.episen.sirius.pcc.back.dto.voyageur;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentImpactDTO {
    private Long id;
    private String message;
    private String ligneNom;
    private int dureeImpactMinutes;
    private boolean enCours;
}