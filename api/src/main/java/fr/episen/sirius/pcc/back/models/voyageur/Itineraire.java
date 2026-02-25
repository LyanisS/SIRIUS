package fr.episen.sirius.pcc.back.models.voyageur;

import fr.episen.sirius.pcc.back.models.regulation.Station;
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

    @ManyToOne
    @JoinColumn(name = "stationdepart", nullable = false)
    private Station stationDepart;

    @ManyToOne
    @JoinColumn(name = "stationarrivee", nullable = false)
    private Station stationArrivee;
}
