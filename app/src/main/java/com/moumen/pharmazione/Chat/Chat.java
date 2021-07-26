package com.moumen.pharmazione.Chat;


import java.util.Date;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

@IgnoreExtraProperties
@Keep
public class Chat extends AbstractChat {
    private String mName;
    private String mMessage;
    private String mUid;
    private String userURL;
    private String userName;
    @ServerTimestamp
    private Date mTimestamp;
    private Boolean seen;

    public Chat() {
        // Needed for Firebase
    }

    public Chat(@Nullable String name, @Nullable String message, @NonNull String uid, @NonNull String userUR, @NonNull String userNam) {
        mName = name;
        mMessage = message;
        mUid = uid;
        userURL = userUR;
        userName = userNam;
        mUid = uid;
        seen = false;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getUserURL() {
        return userURL;
    }

    public void setUserURL(String userURL) {
        this.userURL = userURL;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    @Override
    @Nullable
    public String getName() {
        return mName;
    }

    @Override
    public void setName(@Nullable String name) {
        mName = name;
    }

    @Override
    @Nullable
    public String getMessage() {
        return mMessage;
    }

    @Override
    public void setMessage(@Nullable String message) {
        mMessage = message;
    }

    @Override
    @NonNull
    public String getUid() {
        return mUid;
    }

    @Override
    public void setUid(@NonNull String uid) {
        mUid = uid;
    }

    @Nullable
    @ServerTimestamp
    public Date getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(@Nullable Date timestamp) {
        mTimestamp = timestamp;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Chat chat = (Chat) o;

        return mTimestamp.equals(chat.mTimestamp)
                && mUid.equals(chat.mUid)
                && (mName == null ? chat.mName == null : mName.equals(chat.mName))
                && (mMessage == null ? chat.mMessage == null : mMessage.equals(chat.mMessage));
    }

    @Override
    public int hashCode() {
        int result = mName == null ? 0 : mName.hashCode();
        result = 31 * result + (mMessage == null ? 0 : mMessage.hashCode());
        result = 31 * result + mUid.hashCode();
        result = 31 * result + mTimestamp.hashCode();
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "Chat{" +
                "mName='" + mName + '\'' +
                ", mMessage='" + mMessage + '\'' +
                ", mUid='" + mUid + '\'' +
                ", mTimestamp=" + mTimestamp +
                '}';
    }
}