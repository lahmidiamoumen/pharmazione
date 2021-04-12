package com.moumen.pharmazione.persistance;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import org.jetbrains.annotations.NotNull;

import java.util.List;

//@IgnoreExtraProperties
@Keep
public class Document {

    public String documentID;

    @ServerTimestamp
    public Timestamp scanned;

    @NonNull
    public String title;

    public String body;

    public String category;

    public String etat;

    public List<String> path;

    public String location;

    public String description;

    public String userID;

    public String userUrl;

    public String userName;

    public Boolean satisfied;


//------------Methods--------


    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
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

    @PropertyName("satisfied")
    @NonNull
    public Boolean getSatisfied() {
        return satisfied;
    }

    public void setSatisfied(Boolean satisfied) {
        this.satisfied = satisfied;
    }


    @PropertyName("title")
    @NonNull
    public String getTitle() {
        return title;
    }



    @PropertyName("category")
    @NonNull
    public String getCategory() {
        return category;
    }



    @NonNull
    @PropertyName("path")
    public List<String> getPath() {
        return path;
    }

    public void setPath(@NonNull List<String> path) {
        this.path = path;
    }



    @NotNull
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

    public void setTitle(String title) {
        this.title = title;
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
