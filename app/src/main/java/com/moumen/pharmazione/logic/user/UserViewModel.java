package com.moumen.pharmazione.logic.user;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.moumen.pharmazione.persistance.Document;
import com.moumen.pharmazione.persistance.User;

public class UserViewModel extends ViewModel {
    MutableLiveData<User> blogListMutableLiveData;
    FirebaseFirestore mFirestore;
    UserRepository blogRepository;

    private MutableLiveData<Document> doc;


    public UserViewModel() {
        blogRepository = new UserRepository();
        blogListMutableLiveData=  blogRepository.getUserMutableLiveData();
        mFirestore = FirebaseFirestore.getInstance();
    }

    public MutableLiveData<User> getLiveBlogData() {
        return blogListMutableLiveData;
    }

    public MutableLiveData<Document> getDocData() {
        if (doc == null) {
            doc = new MutableLiveData<>();
        }
        return doc;
    }
}