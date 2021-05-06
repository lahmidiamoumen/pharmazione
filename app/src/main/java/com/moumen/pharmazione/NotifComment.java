package com.moumen.pharmazione;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Bundle;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.moumen.pharmazione.persistance.Document;
import com.moumen.pharmazione.ui.home.ShowFragment;

import static com.moumen.pharmazione.utils.Util.PATH;

public class NotifComment extends AppCompatActivity {
    private SharedViewModel sharedViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notif_comment);
        sharedViewModel =  new ViewModelProvider(this).get(SharedViewModel.class);
        FirebaseApp.initializeApp(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String someData = bundle.getString("id_publication");
            Task<DocumentSnapshot> task = FirebaseFirestore.getInstance().collection(PATH).document(someData).get();
            task.addOnSuccessListener(documentSnapshot -> {
                Document user = documentSnapshot.toObject(Document.class);
                sharedViewModel.getDocData().setValue(user);
                Fragment newFragment = new ShowFragment();

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(android.R.id.content, newFragment)
                        //.addToBackStack(null)
                        .disallowAddToBackStack()   // add to manager " will remember this fragment  - for navigation purpose"
                        .commit();
            });
        }
    }
}