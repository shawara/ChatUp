package com.example.shawara.chat;


import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.example.shawara.chat.utils.ImagePipelineConfigFactory;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Includes one-time initialization of Firebase related code
 */
public class ChatApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        //Firebase.setAndroidContext(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Fresco.initialize(this, ImagePipelineConfigFactory.getImagePipelineConfig(this));
    }

}