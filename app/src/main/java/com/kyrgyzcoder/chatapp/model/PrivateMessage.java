package com.kyrgyzcoder.chatapp.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class PrivateMessage {

    private String senderUid;
    private String receiverUid;
    private String senderName;
    private String message;
    private Boolean isRead;
    private Timestamp sentTimeStr;
    @ServerTimestamp
    private Timestamp sentTime;


    public PrivateMessage(String senderUid, String receiverUid, String senderName, String message, Boolean isRead, Timestamp sentTimeStr) {
        this.senderUid = senderUid;
        this.receiverUid = receiverUid;
        this.senderName = senderName;
        this.message = message;
        this.isRead = isRead;
        this.sentTimeStr = sentTimeStr;
    }

    public PrivateMessage() {
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean read) {
        isRead = read;
    }

    public Timestamp getSentTimeStr() {
        return sentTimeStr;
    }

    public void setSentTimeStr(Timestamp sentTimeStr) {
        this.sentTimeStr = sentTimeStr;
    }

    public Timestamp getSentTime() {
        return sentTime;
    }

    public void setSentTime(Timestamp sentTime) {
        this.sentTime = sentTime;
    }

    @Override
    public String toString() {
        return "PrivateMessage{" +
                "senderUid='" + senderUid + '\'' +
                ", receiverUid='" + receiverUid + '\'' +
                ", senderName='" + senderName + '\'' +
                ", message='" + message + '\'' +
                ", isRead=" + isRead +
                ", sentTimeStr=" + sentTimeStr +
                ", sentTime=" + sentTime +
                '}';
    }
}
