package com.moumen.pharmazione.persistance;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.moumen.pharmazione.utils.Util.PATH_USER;

public class UserState {

    FirebaseUser firebaseUser = null;
    static User user = null;
    static {
        Task<DocumentSnapshot> task =  FirebaseFirestore.getInstance().collection(PATH_USER).document(FirebaseAuth.getInstance().getUid()).get();
        task.addOnSuccessListener(documentSnapshot -> user = documentSnapshot.toObject(User.class));
    }

    public UserState() {}

    public FirebaseUser getFirebaseUser() {
        if( firebaseUser == null) {
            firebaseUser =  FirebaseAuth.getInstance().getCurrentUser();
        }
        return firebaseUser;
    }

    public void setFirebaseUser(FirebaseUser firebaseUser) {
        this.firebaseUser = firebaseUser;
    }

    public static User getUser() {
        return user;
    }

    public void setUser(User user) {
        UserState.user = user;
    }
}
