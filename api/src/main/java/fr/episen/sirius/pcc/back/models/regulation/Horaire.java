package fr.episen.sirius.pcc.back.models.regulation;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "horaire")
public class Horaire {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "datearriveetheorique", nullable = false)
    private Date dateArriveeTheorique;

    @Column(name = "datearriveereelle")
    private Date dateArriveeReelle;

    @Column(name = "datedeparttheorique", nullable = false)
    private Date dateDepartTheorique;

    @Column(name = "datedepartreelle")
    private Date dateDepartReelle;

    @ManyToOne
    @JoinColumn(name = "trajet", nullable = false)
    private Trajet trajet;

    @ManyToOne
    @JoinColumn(name = "lignestation", nullable = false)
    private LigneStation ligneStation;
}
