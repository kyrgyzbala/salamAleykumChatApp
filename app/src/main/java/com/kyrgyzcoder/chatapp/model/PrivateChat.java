package com.kyrgyzcoder.chatapp.model;

import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class PrivateChat {
    private String uid1;
    private String uid2;
    private ArrayList<String> chatters;
    private Timestamp lastMessage;


    public PrivateChat(String uid1, String uid2, ArrayList<String> chatters, Timestamp lastMessage) {
        this.uid1 = uid1;
        this.uid2 = uid2;
        this.chatters = chatters;
        this.lastMessage = lastMessage;
    }

    public PrivateChat() {
    }

    public String getUid1() {
        return uid1;
    }

    public void setUid1(String uid1) {
        this.uid1 = uid1;
    }

    public String getUid2() {
        return uid2;
    }

    public void setUid2(String uid2) {
        this.uid2 = uid2;
    }

    public ArrayList<String> getChatters() {
        return chatters;
    }

    public void setChatters(ArrayList<String> chatters) {
        this.chatters = chatters;
    }

    public Timestamp getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Timestamp lastMessage) {
        this.lastMessage = lastMessage;
    }

    @Override
    public String toString() {
        return "PrivateChat{" +
                "uid1='" + uid1 + '\'' +
                ", uid2='" + uid2 + '\'' +
                ", chatters=" + chatters +
                ", lastMessage=" + lastMessage +
                '}';
    }
}
