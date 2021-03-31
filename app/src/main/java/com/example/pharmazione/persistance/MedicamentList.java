package com.example.pharmazione.persistance;

import androidx.annotation.Keep;

import com.google.firebase.firestore.DocumentId;

@Keep
public class MedicamentList {

    @DocumentId
    public String documentId;
    public String nom;
    public String form;
    public String dosage;
    public Long id;
    public String image;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    //    public String code;
//    public String denomination;
//    public String cond;
//    public String duree;
//    public String num_enregistrement;
//    public String pays_labo;
//    public String labo;


    public MedicamentList(String nom, String form, String dosage, Long id) {
        this.nom = nom;
        this.form = form;
        this.dosage = dosage;
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MedicamentList() {}
}
