package com.example.pharmazione.persistance;

import android.graphics.drawable.Drawable;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

@Keep
public class HorizantallContent {

    public String ID;

    @NonNull
    public  String mLocation;

    @NonNull
    public String mName;

    public String mPhoto;

    public String mDescription;

    public HorizantallContent(String ID,String mLocation, @NonNull String mName, String mPhoto, String mDescription) {
        this.ID = ID;
        this.mLocation = mLocation;
        this.mName = mName;
        this.mPhoto = mPhoto;
        this.mDescription = mDescription;
    }

    public HorizantallContent() {
    }
}
