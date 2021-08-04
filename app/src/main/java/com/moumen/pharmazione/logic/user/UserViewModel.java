package com.moumen.pharmazione.logic.user;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.moumen.pharmazione.persistance.Document;
import com.moumen.pharmazione.persistance.User;

import javax.annotation.Nullable;

public class UserViewModel extends ViewModel {
    FirebaseFirestore mFirestore;
    UserRepository blogRepository;

    private MutableLiveData<Document> doc;


    public UserViewModel() {
        blogRepository = new UserRepository();
        mFirestore = FirebaseFirestore.getInstance();
    }

    public MutableLiveData<User> getLiveBlogData() {
        return blogRepository.getUserMutableLiveData(null);
    }

    public MutableLiveData<User> getLiveBlogData(String uid) {
        return blogRepository.getUserMutableLiveData(uid);
    }

    public MutableLiveData<Document> getDocData() {
        if (doc == null) {
            doc = new MutableLiveData<>();
        }
        return doc;
    }
}