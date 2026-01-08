package fr.episen.sirius.pcc.back.models.regulation;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.sql.Time;
import java.util.Set;

@Entity
@Data
@Table(name = "frequence")
public class Frequence {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recurrence", nullable = false)
    private int recurrence;

    @Column(name = "datedebut", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dateDebut;

    @Column(name = "datefin", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dateFin;

    @Column(name = "heuredebut", nullable = false)
    private Time heureDebut;

    @Column(name = "heurefin", nullable = false)
    private Time heureFin;

    @Column(name = "sens", nullable = false)
    private Boolean sens;

    @ManyToOne
    @JoinColumn(name = "ligne", nullable = false)
    private Ligne ligne;

    @ManyToMany
    @JoinTable(
            name = "frequence_jour",
            joinColumns = @JoinColumn(name = "frequence"),
            inverseJoinColumns = @JoinColumn(name = "jour")
    )
    private Set<Jour> jours;
}
