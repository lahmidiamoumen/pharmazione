package com.moumen.pharmazione.persistance;

import androidx.annotation.Keep;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;

@Keep
public class User implements Serializable {

    @DocumentId
    public String userId;
    public String mEmail;
    public String mPhoneNumber;
    public String mName;
    public String nomOffificine;
    private String addresseOfficine;
    public String mPhotoUri;
    private String token;
    public String wilaya;
    private String type;
    private String fournisseure;
    public Boolean satisfied;
    @ServerTimestamp
    public Timestamp created;
    private Boolean convention_cnas;
    private Boolean convention_casnos;
    private Boolean convention_militair;
    private String carte;


    public User(String mEmail, String mPhoneNumber, String mName, String mPhotoUri) {
        this.mEmail = mEmail;
        this.mPhoneNumber = mPhoneNumber;
        this.mName = mName;
        this.mPhotoUri = mPhotoUri;
    }

    public Boolean getConvention_cnas() {
        return convention_cnas;
    }

    public void setConvention_cnas(Boolean convention_cnas) {
        this.convention_cnas = convention_cnas;
    }

    public Boolean getConvention_casnos() {
        return convention_casnos;
    }

    public void setConvention_casnos(Boolean convention_casnos) {
        this.convention_casnos = convention_casnos;
    }

    public Boolean getConvention_militair() {
        return convention_militair;
    }

    public void setConvention_militair(Boolean convention_militair) {
        this.convention_militair = convention_militair;
    }

    public String getCarte() {
        return carte;
    }


    public void setCarte(String carte) {
        this.carte = carte;
    }

    public String getAddresseOfficine() {
        return addresseOfficine;
    }

    public void setAddresseOfficine(String addresseOfficine) {
        this.addresseOfficine = addresseOfficine;
    }

    public String getFournisseure() {
        return fournisseure;
    }

    public void setFournisseure(String fournisseure) {
        this.fournisseure = fournisseure;
    }

    public String getNomOffificine() {
        return nomOffificine;
    }

    public void setNomOffificine(String nomOffificine) {
        this.nomOffificine = nomOffificine;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public Boolean getSatisfied() {
        return satisfied;
    }

    public void setSatisfied(Boolean satisfied) {
        this.satisfied = satisfied;
    }

    public User() {}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getWilaya() {
        return wilaya;
    }

    public void setWilaya(String wilaya) {
        this.wilaya = wilaya;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getmEmail() {
        return mEmail;
    }

    public void setmEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public String getmPhoneNumber() {
        return mPhoneNumber;
    }

    public void setmPhoneNumber(String mPhoneNumber) {
        this.mPhoneNumber = mPhoneNumber;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmPhotoUri() {
        return mPhotoUri;
    }

    public void setmPhotoUri(String mPhotoUri) {
        this.mPhotoUri = mPhotoUri;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", mEmail='" + mEmail + '\'' +
                ", mPhoneNumber='" + mPhoneNumber + '\'' +
                ", mName='" + mName + '\'' +
                ", mPhotoUri='" + mPhotoUri + '\'' +
                ", token='" + token + '\'' +
                ", wilaya='" + wilaya + '\'' +
                ", type='" + type + '\'' +
                ", satisfied=" + satisfied +
                '}';
    }
}
