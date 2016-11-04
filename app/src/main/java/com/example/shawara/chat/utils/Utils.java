package com.example.shawara.chat.utils;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ServerValue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Utility class
 */
public class Utils {
    /**
     * Format the date with SimpleDateFormat
     */
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyy");
    public static final SimpleDateFormat SIMPLE_TIME_FORMAT = new SimpleDateFormat("hh:mm a");
    private Context mContext = null;


    /**
     * Public constructor that takes mContext for later use
     */
    public Utils(Context con) {
        mContext = con;
    }

    public static String getUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
            // User is signed in
        } else {
            return null;
            // No user is signed in
        }
    }


    public static String getRoomName(String uid1, String uid2) {
        if (uid1.compareTo(uid2) < 0) {
            return uid1 + uid2;
        } else {
            return uid2 + uid1;
        }
    }


    public static String getRelativeDate(long date) {
/*
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);

        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());

        if (cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
            if (cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
                return SIMPLE_TIME_FORMAT.format(date).replace(".", "");
            }else if(cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)-1){

            }
        }
        cal.get(Calendar.DAY_OF_MONTH);*/
        long now = System.currentTimeMillis();
        return DateUtils.getRelativeTimeSpanString(date, now, DateUtils.DAY_IN_MILLIS).toString();
    }


    public static String getRelativeTime(long date) {
        String str = getRelativeDate(date);
        if (str.equals("Today")) return SIMPLE_TIME_FORMAT.format(date).replace(".", "");
        else
            return str;
    }


}
