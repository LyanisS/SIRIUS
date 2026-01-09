package fr.episen.sirius.pcc.back.models.regulation;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "jour")
public class Jour {
    public static final Jour LUNDI = new Jour(1L, "Lundi");
    public static final Jour MARDI = new Jour(1L, "Mardi");
    public static final Jour MERCREDI = new Jour(1L, "Mercredi");
    public static final Jour JEUDI = new Jour(1L, "Jeudi");
    public static final Jour VENDREDI = new Jour(1L, "Vendredi");
    public static final Jour SAMEDI = new Jour(1L, "Samedi");
    public static final Jour DIMANCHE = new Jour(1L, "Dimanche");

    public static Jour getByIndex(int day) {
        assert day > 0;
        assert day < 8;
        if (day == 1) return Jour.LUNDI;
        if (day == 2) return Jour.MARDI;
        if (day == 3) return Jour.MERCREDI;
        if (day == 4) return Jour.JEUDI;
        if (day == 5) return Jour.VENDREDI;
        if (day == 6) return Jour.SAMEDI;
        return Jour.DIMANCHE;
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, unique = true, length = 20)
    private String nom;

    private Jour(Long id, String nom) {
        this.id = id;
        this.nom = nom;
    }

    protected Jour() {}


}
