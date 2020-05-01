package com.kyrgyzcoder.chatapp.model;

import java.io.Serializable;

public class Profile implements Serializable {

    private String displayName;
    private String uid;
    private String photoUrl;
    private String phoneNumber;
    private String status;

    public Profile(String displayName, String uid, String photoUrl, String phoneNumber, String status) {
        this.displayName = displayName;
        this.uid = uid;
        this.photoUrl = photoUrl;
        this.phoneNumber = phoneNumber;
        this.status = status;
    }

    public Profile() {
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Profile{" +
                "displayName='" + displayName + '\'' +
                ", uid='" + uid + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
