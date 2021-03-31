package com.example.pharmazione.persistance;

import android.content.res.Configuration;
import android.graphics.drawable.Drawable;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

@Keep
public class Doctors {

    public String mPhone;

    public String ID;

    @NonNull
    public  String mLocation;

    public String mLocationUrl;

    @NonNull
    public String mName;

    public String mExperience;

    public String mCategory;

    public Drawable mPhoto;

    public String mDescription;

    public String mRating;

    public String mCalbeder;


    public Doctors(String ID, @NonNull String mLocation, @NonNull String mName, Drawable mPhoto, String mDescription, String mCategory, String mExperience,String mLocationUrl) {
        this.ID = ID;
        this.mLocation = mLocation;
        this.mName = mName;
        this.mPhoto = mPhoto;
        this.mDescription = mDescription;
        this.mCategory = mCategory;
        this.mExperience = mExperience;
        //streets-v11
        this.mLocationUrl = "https://api.mapbox.com/styles/v1/mapbox/streets-v11/static/pin-s-hospital+285A98("+mLocationUrl+")/" + mLocationUrl +
                ",13,0,45/600x300@2x?access_token=pk.eyJ1IjoibGFobWlkaW1vdW1lbiIsImEiOiJja2RwM3J6bXoxeHRyMnNyb2dtdnFnanJwIn0.ZsordjuWRvoORjuqU44PPg&attribution=false&logo=false";
    }

    public Doctors() {
    }

    private String mapBuilder(String cors,String zoom,String mapSize,String apiKey,String mapType){

        return "https://api.mapbox.com/styles/v1/mapbox/" +
                mapType +
                "/static/pin-s-hospital+285A98("+cors+")/" + cors +
                ","+zoom+",0" +
                "/" + mapSize +
                "@2x?access_token="+apiKey +
                "&attribution=false&logo=false";
    }
}
