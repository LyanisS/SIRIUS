package fr.episen.sirius.pcc.back.models.voyageur;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "utilisateur")
public class Utilisateur {
    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "motdepasse", nullable = false)
    private String motDePasse;
}
