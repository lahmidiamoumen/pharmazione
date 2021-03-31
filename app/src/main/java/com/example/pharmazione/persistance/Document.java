package com.example.pharmazione.persistance;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

//@IgnoreExtraProperties
@Keep
public class Document {

    public String documentID;

    @ServerTimestamp
    public Timestamp scanned;

    public String expirationDate;

    public String ordPath;


    public  String lotNumber;

    @NonNull
    public String name;

    public String medicamentID;

    @NonNull
    public String category;

    public String etat;

    @NonNull
    public String path;

    @NonNull
    public String location;

    public String description;

    public String userID;

    public String userUrl;

    public String userName;

    public Boolean isVerified;

    public Boolean satisfied;


//------------Methods--------


    public String getMedicamentID() {
        return medicamentID;
    }

    public void setMedicamentID(String medicamentID) {
        this.medicamentID = medicamentID;
    }

    @PropertyName("etat")
    public String getEtat() {
        return etat;
    }



    @PropertyName("documentID")
    public String getDocumentID() {
        return documentID;
    }


    @PropertyName("userName")
    public String getUserName() {
        return userName;
    }


    @PropertyName("userUrl")
    public String getUserUrl() {
        return userUrl;
    }

    public void setUserUrl(String userUrl) {
        this.userUrl = userUrl;
    }
    @PropertyName("userID")
    public String getUserID() {
        return userID;
    }


    public void setScanned(Timestamp scanned) {
        this.scanned = scanned;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    @PropertyName("isVerified")
    @NonNull
    public Boolean getVerified() {
        return isVerified;
    }

    public void setVerified(Boolean verified) {
        isVerified = verified;
    }

    @PropertyName("satisfied")
    @NonNull
    public Boolean getSatisfied() {
        return satisfied;
    }

    public void setSatisfied(Boolean satisfied) {
        this.satisfied = satisfied;
    }

    @PropertyName("ordPath")
    public String getOrdPath() {
        return ordPath;
    }

    public void setOrdPath(String ordPath) {
        this.ordPath = ordPath;
    }


    @PropertyName("name")
    @NonNull
    public String getName() {
        return name;
    }



    @PropertyName("category")
    @NonNull
    public String getCategory() {
        return category;
    }



    @NonNull
    @PropertyName("path")
    public String getPath() {
        return path;
    }

    public void setPath(@NonNull String path) {
        this.path = path;
    }



    @PropertyName("location")
    public String getLocation() {
        return location;
    }


    public void setLocation(@NonNull String pageCount) {
        this.location = pageCount;
    }

    @PropertyName("scanned")
    public Timestamp getScanned() {
        return scanned;
    }

    @PropertyName("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }


    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setDocumentID(String id) {
        this.documentID = id;
    }

    public void setEtat(String s) {
        etat = s;
    }
}
