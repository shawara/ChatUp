package com.example.shawara.chat.utils;

import com.cloudinary.Cloudinary;
import com.example.shawara.chat.BuildConfig;

/**
 * Constants class store most important strings and paths of the app
 */
public final class Constants {
    public static final String CLOUDINARY_URL = "cloudinary://194932669464515:YnsL31MPtvv5K-x5DbyYAyVgs98@du8c5wzvh";
    public static final String FIREBASE_LOCATION_USERS = "users";
    public static final String FIREBASE_LOCATION_MESSAGES = "messages";
    public static final String FIREBASE_LOCATION_LAST_MESSAGE = "last_message";
    public static final String FIREBASE_LOCATION_TYPING = "typing";
    public static final String FIREBASE_LOCATION_FRIENDS = "friends";
    public static final String FIREBASE_LOCATION_MEMBERS = "members";


    /**
     * Constants for Firebase Database URL
     */
    public static final String FIREBASE_URL = BuildConfig.UNIQUE_FIREBASE_DATABASE_ROOT_URL;

    public static final String FIREBASE_URL_USERS = FIREBASE_URL + "/" + FIREBASE_LOCATION_USERS;
    public static final String FIREBASE_URL_MESSAGES = FIREBASE_URL + "/" + FIREBASE_LOCATION_MESSAGES;
    public static final String FIREBASE_URL_LAST_MESSAGE = FIREBASE_URL + "/" + FIREBASE_LOCATION_LAST_MESSAGE;
    public static final String FIREBASE_URL_FRIENDS = FIREBASE_URL + "/" + FIREBASE_LOCATION_FRIENDS;


    /**
     * Constants for Firebase Storage URL
     */
    public static final String FIREBASE_STORAGE_URL = BuildConfig.UNIQUE_FIREBASE_STORAGE_ROOT_URL;
    public static final String FIREBASE_STORAGE_URL_USERS = FIREBASE_STORAGE_URL + "/" + FIREBASE_LOCATION_USERS;
    public static final String FIREBASE_STORAGE_URL_MESSAGES = FIREBASE_STORAGE_URL + "/" + FIREBASE_LOCATION_MESSAGES;


    /**
     * Constants related to locations in Firebase, such as the name of the node
     * where active lists are stored (ie "activeLists")
     */
    public static final String PASSWORD_PROVIDER = "password";


    /**
     * Constants for Firebase object properties
     */
    public static boolean isDestroyed = true;


    public static final String FIREBASE_PROPERTY_TIMESTAMP = "timestamp";
    public static final String FIREBASE_PROPERTY_COUNT = "count";
    public static final String FIREBASE_PROPERTY_ID = "id";
    public static final String KEY_GOOGLE_EMAIL = "GOOGLE_EMAIL";
    public static final String GOOGLE_PROVIDER = "google";


    /**
     * Constants for bundles, extras and shared preferences keys
     */


}
