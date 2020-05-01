package com.kyrgyzcoder.chatapp.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;


public class Request {
    private String senderUid;
    private String receiverUid;
    private String senderName;
    private Boolean answered;
    @ServerTimestamp
    private Timestamp sentTime;

    public Request(String senderUid, String receiverUid, String senderName, Boolean answered) {
        this.senderUid = senderUid;
        this.receiverUid = receiverUid;
        this.senderName = senderName;
        this.answered = answered;
    }

    public Request() {
    }

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public String getReceiverUid() {
        return receiverUid;
    }

    public void setReceiverUid(String receiverUid) {
        this.receiverUid = receiverUid;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public Boolean getAnswered() {
        return answered;
    }

    public void setAnswered(Boolean answered) {
        this.answered = answered;
    }

    public Timestamp getSentTime() {
        return sentTime;
    }

    public void setSentTime(Timestamp sentTime) {
        this.sentTime = sentTime;
    }

    @Override
    public String toString() {
        return "Request{" +
                "senderUid='" + senderUid + '\'' +
                ", receiverUid='" + receiverUid + '\'' +
                ", senderName='" + senderName + '\'' +
                ", answered='" + answered + '\'' +
                ", sentTime=" + sentTime +
                '}';
    }
}
