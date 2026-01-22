package fr.episen.sirius.pcc.back.dto.regulation;


import lombok.Data;

import java.util.Date;

@Data
public class UpdateIncidentDTO {
    private String message;

    private Date dateDebut;

    private Date dateFin;

    private Long trajetId;
}
