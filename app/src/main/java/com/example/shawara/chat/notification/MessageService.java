package com.example.shawara.chat.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import com.example.shawara.chat.R;
import com.example.shawara.chat.model.MessageObject;
import com.example.shawara.chat.model.User;
import com.example.shawara.chat.ui.ChatActivity;
import com.example.shawara.chat.ui.home.ChatListFragment.ChatItem;
import com.example.shawara.chat.ui.home.HomeActivity;
import com.example.shawara.chat.utils.Constants;
import com.example.shawara.chat.utils.Utils;
import com.example.shawara.chat.widget.CircleTransform;
import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;


/**
 * Created by shawara on 9/29/2016.
 */

public class MessageService extends Service {
    private static final String TAG = "MessageService";
    private ChildEventListener sMessageListener;
    private DatabaseReference sLastMessageRef;
    private DatabaseReference mFriendLastMessagesRef;
    private DatabaseReference mUsersRef;
    private DatabaseReference mChatRef;
    private Bitmap mDefaultBitmap;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        mDefaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_profile);
        mDefaultBitmap = new CircleTransform().transform(mDefaultBitmap);
        initFireBase();
        userPresence();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        // Toast.makeText(getBaseContext(), "Started", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        if (sMessageListener != null && sLastMessageRef != null)
            sLastMessageRef.removeEventListener(sMessageListener);
        super.onDestroy();
    }

    private void initFireBase() {
        sLastMessageRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(Constants.FIREBASE_URL_LAST_MESSAGE).child(Utils.getUid());
        sLastMessageRef.keepSynced(true);

        mFriendLastMessagesRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(Constants.FIREBASE_URL_LAST_MESSAGE);
        mFriendLastMessagesRef.keepSynced(true);


        mUsersRef = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.FIREBASE_URL_USERS);
        mChatRef = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.FIREBASE_URL_MESSAGES);
        mChatRef.keepSynced(true);

        if (sMessageListener != null && sLastMessageRef != null)
            sLastMessageRef.removeEventListener(sMessageListener);

        sMessageListener = getLastMessageChildEventListener();

        sLastMessageRef.addChildEventListener(sMessageListener);


    }


    private void handleLastMessage(DataSnapshot dataSnapshot) {
        long count = (long) dataSnapshot.child(Constants.FIREBASE_PROPERTY_COUNT).getValue();
        if (count < 1) return;
        String userId = dataSnapshot.getKey();
        String messageId = dataSnapshot.child(Constants.FIREBASE_PROPERTY_ID).getValue().toString();
        ChatItem chatItem = new ChatItem(userId, messageId, count);
        Log.d(TAG, "onChildAdded: " + count + " messageId:" + messageId + " userID:" + userId);

        getUser(chatItem);
    }

    private ChildEventListener getLastMessageChildEventListener() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                handleLastMessage(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                handleLastMessage(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }


    private void getUser(final ChatItem chatItem) {
        mUsersRef.child(chatItem.user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                user.setUid(dataSnapshot.getKey());
                chatItem.user = user;
                getMessage(chatItem);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void getMessage(final ChatItem chatItem) {
        final String chatRoomId = Utils.getRoomName(Utils.getUid(), chatItem.user.getUid());

        mChatRef.child(chatRoomId + "/" + Constants.FIREBASE_LOCATION_MESSAGES + "/" + chatItem.message.getMessageID())
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //when message not synced yet.
                                if (!dataSnapshot.exists()) {
                                    Log.d(TAG, "key=" + dataSnapshot.getKey() + " doesn't exist");
                                    return;
                                }

                                MessageObject message = dataSnapshot.getValue(MessageObject.class);
                                message.setMessageID(dataSnapshot.getKey());
                                chatItem.message = message;

                                if (chatItem.user.getProfileImageUrl() != null) {
                                    //new ProfileImageDownloader(chatItem).execute();
                                    FrescoHandler(Uri.parse(chatItem.user.getProfileImageUrl()), chatItem);
                                } else {
                                    showNotification(chatItem, mDefaultBitmap);
                                }

                                //remove message listener
                                mChatRef.child(chatRoomId + "/"
                                        + Constants.FIREBASE_LOCATION_MESSAGES + "/"
                                        + chatItem.message.getMessageID()).removeEventListener(this);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d(TAG, "onCancelled:" + databaseError);
                            }
                        }

                );
    }


    private void FrescoHandler(Uri uri, final ChatItem item) {
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>>
                dataSource = imagePipeline.fetchDecodedImage(Utils.getImageRequest(uri), getBaseContext());
        dataSource.subscribe(new BaseBitmapDataSubscriber() {
                                 @Override
                                 public void onNewResultImpl(@Nullable Bitmap bitmap) {
                                     // You can use the bitmap here, but in limited ways.
                                     // No need to do any cleanup.
                                     Log.d(TAG, "Fresco Did it OMG!");
                                     showNotification(item, new CircleTransform().transform(bitmap));
                                 }

                                 @Override
                                 public void onFailureImpl(DataSource dataSource) {
                                     // No cleanup required here.
                                     showNotification(item, mDefaultBitmap);
                                 }
                             },
                CallerThreadExecutor.getInstance());
    }

//    private class ProfileImageDownloader extends AsyncTask<Void, Void, Bitmap> {
//        private ChatItem mChatItem;
//
//        public ProfileImageDownloader(ChatItem chatItem) {
//            mChatItem = chatItem;
//        }
//
//        @Override
//        protected Bitmap doInBackground(Void... param) {
//            Bitmap bmp = null;
//            try {
//                bmp = ImageDownloader.getUrlBitmap(mChatItem.user.getProfileImageUrl());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return bmp;
//        }
//
//        @Override
//        protected void onPostExecute(Bitmap bitmap) {
//            if (bitmap == null) {
//                //  Bitmap larg = BitmapFactory.decodeResource(getResources(), R.drawable.default_profile);
//                showNotification(mChatItem, mDefaultBitmap);
//            } else {
//                showNotification(mChatItem, new CircleTransform().transform(bitmap));
//            }
//        }
//    }


    private void showNotification(ChatItem chatItem, Bitmap bitmap) {
        Log.d(TAG, "showNotification: ");
        Bitmap largeIcon = bitmap;
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        int color = getResources().getColor(R.color.icon_color);


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.app_icon)
                        .setColor(color)
                        .setLargeIcon(largeIcon)
                        .setSound(alarmSound)
                        .setContentTitle(chatItem.user.getName())
                        .setContentText(chatItem.message.getMessage())
                        .setAutoCancel(true)
                        .setPriority(Notification.PRIORITY_MAX);


        lighScreen();


        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = ChatActivity.newIntent(getBaseContext(), chatItem.user);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(HomeActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;

        mNotificationManager.notify(chatItem.user.getUid().hashCode(), notification);

        if (chatItem.message.getState() != MessageObject.STATE_SEEN)
            mChatRef.child(Utils.getRoomName(Utils.getUid(), chatItem.user.getUid()) + "/"
                    + Constants.FIREBASE_LOCATION_MESSAGES + "/"
                    + chatItem.message.getMessageID()).child("state").setValue(MessageObject.STATE_DELIVERED);

        sLastMessageRef.child(chatItem.user.getUid()).child(Constants.FIREBASE_PROPERTY_COUNT).setValue(-1000 - chatItem.count);
        mFriendLastMessagesRef
                .child(chatItem.user.getUid())
                .child(Utils.getUid())
                .child(Constants.FIREBASE_PROPERTY_COUNT).setValue(-9);
        //startForeground(chatItem.user.getUid().hashCode(), notification);
    }

    private void lighScreen() {
        PowerManager pm = (PowerManager) getBaseContext().getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        Log.e("screen on", "" + isScreenOn);
        if (isScreenOn == false) {
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "MyLock");
            wl.acquire(10000);
            PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyCpuLock");
            wl_cpu.acquire(10000);
        }
    }

    private void userPresence() {
        // since I can connect from multiple devices, we store each connection instance separately
        // any time that connectionsRef's value is null (i.e. has no children) I am offline
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myConnectionsRef = database.getReference("/status/" + Utils.getUid() + "/connections");

        // stores the timestamp of my last disconnect (the last time I was seen online)
        final DatabaseReference lastOnlineRef = database.getReference("/status/" + Utils.getUid() + "/lastOnline");

        final DatabaseReference connectedRef = database.getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    // add this device to my connections list
                    // this value could contain info about the device or a timestamp too
                    // DatabaseReference con = myConnectionsRef.push();
                    myConnectionsRef.setValue(Boolean.TRUE);

                    // when this device disconnects, remove it
                    myConnectionsRef.onDisconnect().removeValue();

                    // when I disconnect, update the last time I was seen online
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled at .info/connected");
            }
        });
    }
}
