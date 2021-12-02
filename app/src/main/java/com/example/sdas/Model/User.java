package com.example.sdas.Model;

import java.util.HashMap;

public class User {

    private String uid;
    private String email;



    private String name;
    private HashMap<String,User> acceptList;

    private String trackStatus;

    public User(){
    }

    public User(String uid, String email, String name) {
        this.uid = uid;
        this.email = email;
        this.name = name;

//        this.acceptList = new HashMap<>();
    }


    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
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


    public void setName(String name) {
        this.name = name;
    }

    public void setAcceptList(HashMap<String, User> acceptList) {
        this.acceptList = acceptList;
    }
}
