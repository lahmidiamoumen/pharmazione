package com.moumen.pharmazione;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.moumen.pharmazione.logic.user.UserViewModel;
import com.moumen.pharmazione.persistance.Document;
import com.moumen.pharmazione.persistance.User;
import com.moumen.pharmazione.ui.home.ShowFragment;
import com.moumen.pharmazione.ui.profile.ProfileFragment;

import static com.moumen.pharmazione.utils.Util.PATH;
import static com.moumen.pharmazione.utils.Util.PATH_USER;

public class ShowProfileActiv extends AppCompatActivity {

    ShowProfileActiv() {
        FirebaseApp.initializeApp(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notif_comment);

//                    FragmentManager fm = getSupportFragmentManager();
//                    newFragment = fm.findFragmentByTag("myFragmentTag");
//                    if (newFragment == null) {
//                        FragmentTransaction ft = fm.beginTransaction();
//                        newFragment = new ProfileFragment();
//                        ft.add(android.R.id.content,newFragment,"myFragmentTag");
//                        ft.commit();
//                    }

        Fragment newFragment = new ProfileFragment();

        getSupportFragmentManager()

                .beginTransaction()
                .replace(android.R.id.content, newFragment)
                //.addToBackStack(null)
                .disallowAddToBackStack()   // add to manager " will remember this fragment  - for navigation purpose"
                .commit();

    }

}