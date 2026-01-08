package fr.episen.sirius.pcc.back.models.regulation;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "train")
public class Train {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vitesse")
    private Float vitesse;

    @Column(name = "datearriveeposition")
    private Date dateArriveePosition;

    @ManyToOne
    @JoinColumn(name = "position", nullable = false)
    private ElementVoie position;
}
