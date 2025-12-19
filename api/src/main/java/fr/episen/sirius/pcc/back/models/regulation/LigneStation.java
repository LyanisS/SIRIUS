package fr.episen.sirius.pcc.back.models.regulation;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "lignestation")
public class LigneStation {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ordre", nullable = false)
    private Integer ordre;

    @ManyToOne
    @JoinColumn(name = "ligne", nullable = false)
    private Ligne ligne;

    @ManyToOne
    @JoinColumn(name = "station", nullable = false)
    private Station station;
}
