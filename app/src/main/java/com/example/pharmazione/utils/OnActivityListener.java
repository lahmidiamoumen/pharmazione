package com.example.pharmazione.utils;

import android.content.Context;
import android.widget.ImageButton;

public interface OnActivityListener {

    void onDetachListener();
    void onAtachListener();
    void onBindNotification(ImageButton v, Context context);

}
