package com.moumen.pharmazione.persistance;

import androidx.annotation.Keep;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.List;

@Keep
public class ChatCollector {

    @DocumentId
    String chatID;

    @ServerTimestamp
    public Timestamp timestamp;
    public Timestamp lastSeen;
    public List<String> chatters;
    public List<String> chattersNames;
    public List<String> chattersUrls;

    public ChatCollector() {}

    public ChatCollector( List<String> chatters,List<String> chattersNames ,List<String> chattersUrls) {
        this.chatters = chatters;
        this.chattersNames = chattersNames;
        this.chattersUrls = chattersUrls;
    }


    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }

    public Timestamp getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Timestamp lastSeen) {
        this.lastSeen = lastSeen;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getChatters() {
        return chatters;
    }

    public void setChatters(List<String> chatters) {
        this.chatters = chatters;
    }
}
