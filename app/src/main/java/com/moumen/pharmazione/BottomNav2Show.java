package com.moumen.pharmazione;

import android.os.Bundle;

import androidx.navigation.Navigation;

import com.moumen.pharmazione.ui.home.ShowFragment;

public class BottomNav2Show extends BottomNavigation {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null){
            navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            // navController.navigateUp();
            navController.navigate(R.id.showFragment);
        }
    }
}
