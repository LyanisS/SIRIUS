package fr.episen.sirius.pcc.back.dto.voyageur;


import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class CreateItineraireFavoriDTO {
    private Date date;

    private boolean depart = true;

    @NotNull(message = "Station Depart ID is required")
    private Long stationDepartId;

    @NotNull(message = "Station Arrivee ID is required")
    private Long stationArriveeId;
}
