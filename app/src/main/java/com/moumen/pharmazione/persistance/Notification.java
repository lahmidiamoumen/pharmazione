package com.moumen.pharmazione.persistance;

import androidx.annotation.Keep;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

@Keep
public class Notification {
    public String title;
    public String content;
    public String toUser;
    public String userID;
    public String userURL;
    public String userName;
    public String publicationId;
    public String commentId;
    public Boolean seen;
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

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }
}
