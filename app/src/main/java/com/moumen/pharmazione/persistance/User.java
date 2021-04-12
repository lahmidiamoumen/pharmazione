package com.moumen.pharmazione.persistance;

import android.net.Uri;

import androidx.annotation.Keep;

import com.google.firebase.firestore.DocumentId;

@Keep

public class User {

    @DocumentId
    private  String userId;
    private  String mEmail;
    private  String mPhoneNumber;
    private  String mName;
    private  String mPhotoUri;

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

    public void setmPhotoUri(Uri mPhotoUri) {
        this.mPhotoUri = mPhotoUri.toString();
    }
}
