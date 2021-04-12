package com.moumen.pharmazione.persistance;

import androidx.annotation.Keep;

@Keep
public class DonBesoin {
    public String donneur_name;
    public String donneur_phone;
    public Boolean seen;
    public String name;
    public String token;
    public String user_id;
    public String path;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDonneur_name() {
        return donneur_name;
    }

    public void setDonneur_name(String donneur_name) {
        this.donneur_name = donneur_name;
    }

    public String getDonneur_phone() {
        return donneur_phone;
    }

    public void setDonneur_phone(String donneur_phone) {
        this.donneur_phone = donneur_phone;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
