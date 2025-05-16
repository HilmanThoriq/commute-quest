package com.example.commutequest.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message {

    public static final int TYPE_INCOMING = 0;
    public static final int TYPE_OUTGOING = 1;

    private String id;
    private String text;
    private long timestamp;
    private int type;
    private boolean isRead;
    private String senderName;

    public Message() {
        // Required empty constructor for Firebase
    }

    public Message(String text, int type) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.text = text;
        this.timestamp = System.currentTimeMillis();
        this.type = type;
        this.isRead = false;
        this.senderName = type == TYPE_INCOMING ? "Bus Assistant" : "You";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
