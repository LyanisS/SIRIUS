package fr.episen.sirius.pcc.back.models.voyageur;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "itineraire")
public class Itineraire {
    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "utilisateur", nullable = false)
    private Utilisateur utilisateur;

    @Column(name="date", nullable = false)
    private Date date;

    @Column(name = "depart", nullable = false)
    private Boolean depart;

    @Column(name = "stationdepart", nullable = false)
    private int stationDepart;

    @Column(name = "stationarrivee", nullable = false)
    private int stationArrivee;
}
