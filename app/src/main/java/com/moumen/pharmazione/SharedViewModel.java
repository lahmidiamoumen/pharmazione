package com.moumen.pharmazione;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.moumen.pharmazione.persistance.Doctors;
import com.moumen.pharmazione.persistance.Document;
import com.moumen.pharmazione.persistance.DonBesoin;
import com.moumen.pharmazione.persistance.HorizantallContent;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QuerySnapshot;
import com.moumen.pharmazione.persistance.User;

import java.util.List;

public class SharedViewModel extends ViewModel {
    private MutableLiveData<Integer> badge;
    private MutableLiveData<EventListener<QuerySnapshot>> eventListenerMutableLiveData;
    private MutableLiveData<User> user;
    private MutableLiveData<Document> doc;
    private MutableLiveData<HorizantallContent> hc;
    private MutableLiveData<Doctors> doctors;
    private LiveData<List<DonBesoin>> donBesoin;

    public MutableLiveData<Integer> getBadge() {
        if (badge == null) {
            badge = new MutableLiveData<>();
        }
        return badge;
    }

    public MutableLiveData<EventListener<QuerySnapshot>> getEventListener() {
        if (eventListenerMutableLiveData == null) {
            eventListenerMutableLiveData = new MutableLiveData<>();
        }
        return eventListenerMutableLiveData;
    }

    public MutableLiveData<Document> getDocData() {
        if (doc == null) {
            doc = new MutableLiveData<>();
        }
        return doc;
    }

    public MutableLiveData<User> getUserData() {
        if (user == null) {
            user = new MutableLiveData<>();
        }
        return user;
    }

    public MutableLiveData<HorizantallContent> getHcData() {
        if (hc == null) {
            hc = new MutableLiveData<>();
        }
        return hc;
    }

    public MutableLiveData<Doctors> getDoctorsData() {
        if (doctors == null) {
            doctors = new MutableLiveData<>();
        }
        return doctors;
    }

    public LiveData<List<DonBesoin>> getDonBesoin() {
        if (donBesoin == null) {
            donBesoin = new MutableLiveData<>();
        }
        return donBesoin;
    }
}
