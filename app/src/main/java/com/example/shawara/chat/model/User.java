package com.example.shawara.chat.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by shawara on 9/14/2016.
 */


public class User implements Serializable, Comparable<User> {
    private String name;
    private String nameCaseIgnore;
    private String email;
    private String profileImageUrl;
    private HashMap<String, Object> timestampJoined;
    private String uid;

    /**
     * Required public constructor
     */
    public User() {
    }

    /**
     * Use this constructor to create new User.
     * Takes user name, email and profileImageUrl as params
     *
     * @param name
     * @param email
     * @param profileImageUrl
     */
    public User(String name, String email, String profileImageUrl) {
        this.name = name;
        nameCaseIgnore = name.toLowerCase();
        this.email = email;
        this.profileImageUrl = profileImageUrl;
    }

    @Exclude
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNameCaseIgnore() {
        return nameCaseIgnore;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setName(String name) {
        this.name = name;
        nameCaseIgnore = name.toLowerCase();
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setTimestampJoined(HashMap<String, Object> timestampJoined) {
        this.timestampJoined = timestampJoined;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public HashMap<String, Object> getTimestampJoined() {
        return timestampJoined;
    }


    @Override
    public int compareTo(User user) {
        return this.getNameCaseIgnore().compareTo(user.getNameCaseIgnore());
    }
}