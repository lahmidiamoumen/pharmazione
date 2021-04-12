package com.moumen.pharmazione.utils;

import android.view.View;


public interface MedClickListener<T> {
    void onClick(View view, T item);
}
