package com.moumen.pharmazione;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
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
    private UserViewModel sharedViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notif_comment);
        sharedViewModel =  new ViewModelProvider(this).get(UserViewModel.class);
        FirebaseApp.initializeApp(this);

        String uid = getIntent().getStringExtra("id_user");
        if(uid != null){
            Task<DocumentSnapshot> task = FirebaseFirestore.getInstance().collection(PATH_USER).document(uid).get();
            task.addOnSuccessListener(documentSnapshot -> {
                User user = documentSnapshot.toObject(User.class);
                sharedViewModel.getLiveBlogData().postValue(user);
                Fragment newFragment = new ProfileFragment();

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(android.R.id.content, newFragment)
                        //.addToBackStack(null)
                        .disallowAddToBackStack()   // add to manager " will remember this fragment  - for navigation purpose"
                        .commit();
            });
        }else {
            System.out.println("Bunddel is emty");
        }
    }

}