package com.moumen.pharmazione.persistance;

import android.net.Uri;

import androidx.annotation.Keep;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

@Keep

public class User {

    @DocumentId
    public  String userId;
    public  String mEmail;
    public  String mPhoneNumber;
    public  String mName;
    public  String mPhotoUri;
    private  String token;
    public  String wilaya;
    private  String type;
    public Boolean satisfied;
    @ServerTimestamp
    public Timestamp created;

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

    public User(String mEmail, String mPhoneNumber, String mName, String mPhotoUri) {
        this.mEmail = mEmail;
        this.mPhoneNumber = mPhoneNumber;
        this.mName = mName;
        this.mPhotoUri = mPhotoUri;
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
