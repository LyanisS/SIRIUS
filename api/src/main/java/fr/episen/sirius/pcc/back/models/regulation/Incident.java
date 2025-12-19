package fr.episen.sirius.pcc.back.models.regulation;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "incident")
public class Incident {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "datedebut", nullable = false)
    private Date dateDebut;

    @Column(name = "datefin")
    private Date dateFin;

    @ManyToOne
    @JoinColumn(name = "trajet", nullable = false)
    private Trajet trajet;
}
