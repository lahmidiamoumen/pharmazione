package com.moumen.pharmazione.logic.user;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.moumen.pharmazione.persistance.User;

import static com.moumen.pharmazione.utils.Util.PATH_USER;

public class UserRepository {

    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;
    MutableLiveData<User> blogMutableLiveData;

    public UserRepository() {
        //define firestore
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        //define bloglist
        blogMutableLiveData = new MutableLiveData<>();
    }

    //get blog from firebase firestore
    public MutableLiveData<User> getUserMutableLiveData(String uid) {
        if(uid == null) {
            if(mAuth.getCurrentUser() != null)
                mFirestore.collection(PATH_USER)
                    .document(mAuth.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        User user = documentSnapshot.toObject(User.class);
                        blogMutableLiveData.setValue(user);
                    });
        } else {
            blogMutableLiveData = new MutableLiveData<>();
                mFirestore.collection(PATH_USER)
                        .document(uid)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            User user = documentSnapshot.toObject(User.class);
                            blogMutableLiveData.setValue(user);
                        });
        }

        return blogMutableLiveData;
    }

}
