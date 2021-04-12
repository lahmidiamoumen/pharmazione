package com.moumen.pharmazione.persistance;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

@Keep
public class Pharamcy {

    public String ID;

    @ServerTimestamp
    public Date added_at;


    @NonNull
    public  String location;

    @NonNull
    public String name;
}
