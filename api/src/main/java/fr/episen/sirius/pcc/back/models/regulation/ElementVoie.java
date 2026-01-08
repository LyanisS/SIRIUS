package fr.episen.sirius.pcc.back.models.regulation;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "elementvoie")
public class ElementVoie {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "longueur", nullable = false)
    private Integer longueur;

    @ManyToOne
    @JoinColumn(name = "elementsuivant")
    private ElementVoie elementSuivant;
}
