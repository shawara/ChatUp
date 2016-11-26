package com.example.shawara.chat.utils;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
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

    public static ImageRequest getImageRequest(Uri uri) {
        ImageRequest request = ImageRequestBuilder
                .newBuilderWithSource(uri)
                .setLocalThumbnailPreviewsEnabled(true)
                .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                .setProgressiveRenderingEnabled(false)
                .build();
        return request;
    }

    public static Spanned getSpannedString(String str, String sub) {
        String lowerCase = str.toLowerCase();
        String stag = "<font color='#2196F3'>", etag = "</font>";

        int atIndx = 0;
        int ind = -1;
        int curInc = 0;
        while ((ind = lowerCase.indexOf(sub, atIndx)) != -1) {
            Log.d("Utils", "getSpannableString: =" + ind);
            str = str.substring(0, ind + curInc)
                    + stag
                    + str.substring(ind + curInc, ind + curInc + sub.length())
                    + etag
                    + str.substring(ind + curInc + sub.length(), str.length());

            atIndx = ind + sub.length();
            curInc += stag.length() + etag.length();
        }

        return Html.fromHtml(str);
    }

}
