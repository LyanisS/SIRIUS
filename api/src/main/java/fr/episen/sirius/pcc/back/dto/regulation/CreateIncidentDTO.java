package fr.episen.sirius.pcc.back.dto.regulation;


import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
public class CreateIncidentDTO {
    @NotBlank(message = "Message is required")
    private String message;

    private Date dateFin;

    @NotNull(message = "Trajet ID is required")
    private Long trajetId;
}
