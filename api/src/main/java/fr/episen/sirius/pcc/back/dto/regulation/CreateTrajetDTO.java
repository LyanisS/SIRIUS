package fr.episen.sirius.pcc.back.dto.regulation;


import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CreateTrajetDTO {
    @NotNull(message = "Ligne ID is required")
    private Long ligneId;

    @NotNull(message = "Train ID is required")
    private Long trainId;
}
