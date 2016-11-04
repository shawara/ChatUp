package com.example.shawara.chat;


import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Includes one-time initialization of Firebase related code
 */
public class ChatApplication extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //Firebase.setAndroidContext(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);


    }


}