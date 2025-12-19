package fr.episen.sirius.pcc.back.models.regulation;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "station")
public class Station {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;
}
