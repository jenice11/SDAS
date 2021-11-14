package com.example.trackingapp.Model;

import java.util.HashMap;

public class User {

    private String uid,email;
    private HashMap<String,User> acceptList;

    public User(){
    }

    public User(String uid, String email) {
        this.uid = uid;
        this.email = email;
        this.acceptList = new HashMap<>();
    }


    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public HashMap<String, User> getAcceptList() {
        return acceptList;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAcceptList(HashMap<String, User> acceptList) {
        this.acceptList = acceptList;
    }
}
