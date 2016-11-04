package com.example.shawara.chat.model;

import android.graphics.drawable.Drawable;

import com.google.firebase.database.Exclude;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by shawara on 4/22/2016.
 */
public class MessageObject {
    private String mMessage;
    private Object mDate;
    private int mState = STATE_SENT;
    private String mMessageID;
    private String mFrom;
    private String mTo;
    private int mMessageType = TEXT;

    public final static int TEXT = 0;
    public final static int IMAGE = 1;
    public final static int VIDEO = 2;

    public final static int STATE_NOT_SENT = 0;
    public final static int STATE_SENT = 1;
    public final static int STATE_DELIVERED = 4;
    public final static int STATE_SEEN = 3;

    public MessageObject() {

    }

    public int getState() {
        return mState;
    }

    public void setState(int state) {
        mState = state;
    }

    @Exclude
    public String getMessageID() {
        return mMessageID;
    }

    public void setMessageID(String messageID) {
        mMessageID = messageID;
    }

    public MessageObject(String message, Object date, int type, String from, String to) {
        this(message, date, from, to);
        mMessageType = type;
    }

    public MessageObject(String message, Object date, String from, String to) {
        mMessage = message;
        mDate = date;
        mFrom = from;
        mTo = to;
    }

    public Object getDate() {
        return mDate;
    }

    public void setDate(Object date) {
        mDate = date;
    }

    public String getTo() {
        return mTo;
    }

    public void setTo(String to) {
        mTo = to;
    }

    public String getFrom() {
        return mFrom;
    }

    public void setFrom(String from) {
        mFrom = from;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public int getMessageType() {
        return mMessageType;
    }

    public void setMessageType(int messageType) {
        mMessageType = messageType;
    }
}


