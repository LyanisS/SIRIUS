package fr.episen.sirius.pcc.back.dto.voyageur;

import lombok.Data;

@Data
public class IncidentImpactDTO {
    public Long id;
    public String message;
    public String ligneNom;
    public int dureeImpactMinutes;
    public boolean enCours;
}
