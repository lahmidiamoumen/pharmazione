package com.example.pharmazione.ui.speciality;

import androidx.annotation.DrawableRes;
import androidx.annotation.Keep;

@Keep
public class Speciality {
    @DrawableRes
    public int mPhoto;
    public String mName;

    public Speciality(@DrawableRes int mPhoto, String mName) {
        this.mPhoto = mPhoto;
        this.mName = mName;
    }
}
