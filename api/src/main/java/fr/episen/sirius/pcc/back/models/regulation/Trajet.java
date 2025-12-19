package fr.episen.sirius.pcc.back.models.regulation;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "trajet")
public class Trajet {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ligne", nullable = false)
    private Ligne ligne;

    @ManyToOne
    @JoinColumn(name = "train", nullable = false)
    private Train train;
}
