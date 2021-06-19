package com.moumen.pharmazione.persistance;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class Notification {
    String title;
    String content;
    String toUser;
    @ServerTimestamp
    public Timestamp created;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }
}
