package edu.ezip.ing1.pds.business.dto;

public class Trajet {

    private int id;
    private String nom;
    private int planningId;
    private int conducteurId;

    public Trajet(int id, String nom, int planningId, int conducteurId) {
        this.id = id;
        this.nom = nom;
        this.planningId = planningId;
        this.conducteurId = conducteurId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getPlanningId() {
        return planningId;
    }

    public void setPlanningId(int planningId) {
        this.planningId = planningId;
    }

    public int getConducteurId() {
        return conducteurId;
    }

    public void setConducteurId(int conducteurId) {
        this.conducteurId = conducteurId;
    }
}
