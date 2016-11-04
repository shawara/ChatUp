package com.example.shawara.chat.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.shawara.chat.R;
import com.example.shawara.chat.widget.CircleTransform;
import com.example.shawara.chat.model.MessageObject;
import com.example.shawara.chat.model.User;
import com.example.shawara.chat.ui.settings.SettingsActivity;
import com.example.shawara.chat.utils.Constants;
import com.example.shawara.chat.utils.ImageUtils;
import com.example.shawara.chat.utils.Utils;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by shawara on 4/21/2016.
 */
public class ChatFragment extends Fragment {
    public final static int CAMERA_REQUEST = 100;
    public final static int PICK_IMAGE_REQUEST = 101;
    public final static int CAMERA_PERMISSION_REQUEST = 1;

    private final static String TAG = "ChatFragment";

    private RecyclerView mRecyclerView;
    private EmojiEditText mMessageBox;
    private List<MessageObject> mMessageList = new ArrayList<>();
    private MessageAdapter mAdapter;
    private FloatingActionButton mFloatingActionButton;
    private ImageView mCameraView;
    private TextView mTitleTextView;
    private TextView mSubTitleTextView;
    private ImageView mActionBarImage;
    private ActionBar actionBar;
    ImageView mSmileImageView;
    private int mMessageBoxState = 0;
    private long friendCounter = 0;

    private File mFile;

    private String mMyUid;
    private User mUser;
    private HashMap<String, Integer> mVisitedMessages = new HashMap<>();

    private DatabaseReference mChatRoomRef;
    private DatabaseReference mLastMessageRef;
    private DatabaseReference mMyLastMessagesRef;
    private DatabaseReference mFriendLastMessagesRef;
    private DatabaseReference mTypingRef;
    private ChildEventListener mChatRoomChildEventListener;
    private ValueEventListener mTypingValueEventListener;
    private ValueEventListener mCounterValueEventListener;

    private Cloudinary mCloudinary;


    EmojiPopup emojiPopup;

    private boolean isFriendOnline = false;
    private boolean isIOnline = false;
    private String lastSeen = "";


    public static ChatFragment newInstance() {
        return new ChatFragment();
    }


    private void traceFriendConnection() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference connectionsRef = database.getReference("/users/" + mUser.getUid() + "/connections");
        final DatabaseReference myConnectionsRef = database.getReference("/users/" + Utils.getUid() + "/connections");

        // stores the timestamp of my last disconnect (the last time I was seen online)
        final DatabaseReference lastOnlineRef = database.getReference("/users/" + mUser.getUid() + "/lastOnline");

        lastOnlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    long date = dataSnapshot.getValue(long.class);
                    lastSeen = Utils.getRelativeDate(date) + " " + Utils.SIMPLE_TIME_FORMAT.format(date);
                    if (!isFriendOnline) {
                        mSubTitleTextView.setVisibility(View.VISIBLE);
                        mSubTitleTextView.setText(lastSeen);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        connectionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                isFriendOnline = dataSnapshot.exists();
                if (isFriendOnline) {
                    mSubTitleTextView.setVisibility(View.VISIBLE);
                    mSubTitleTextView.setText("online");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        myConnectionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                isIOnline = (dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    // adding profile photo and name to action bar
    private void initActionBarWithUserData() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        actionBar = activity.getSupportActionBar();
        View customView = LayoutInflater.from(getActivity()).inflate(R.layout.profile_cyrcle, null);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(customView);

        mActionBarImage = (ImageView) customView.findViewById(R.id.profile_photo);
        mTitleTextView = (TextView) customView.findViewById(R.id.chat_title_text_view);
        mSubTitleTextView = (TextView) customView.findViewById(R.id.chat_subtitle_text_view);

        mTitleTextView.setText(mUser.getName());
        mSubTitleTextView.setVisibility(View.GONE);

        Picasso.with(getContext()).load(mUser.getProfileImageUrl())
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile).into(mActionBarImage);
    }


    // declare references on chat room
    private void declareFirebase() {
        mUser = (User) getActivity().getIntent().getSerializableExtra(ChatActivity.EXTRA_USER);
        mMyUid = Utils.getUid();


        String chatRoomUrl = Constants.FIREBASE_URL_MESSAGES + "/" + Utils.getRoomName(mMyUid, mUser.getUid());

        mChatRoomRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(chatRoomUrl + "/" + Constants.FIREBASE_LOCATION_MESSAGES);

        mLastMessageRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(Constants.FIREBASE_URL_LAST_MESSAGE);

        mMyLastMessagesRef = mLastMessageRef.child(mMyUid).child(mUser.getUid());
        mFriendLastMessagesRef = mLastMessageRef.child(mUser.getUid()).child(mMyUid);

        mChatRoomRef.keepSynced(true);

        mTypingRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(chatRoomUrl + "/" + Constants.FIREBASE_LOCATION_TYPING);


        mCounterValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friendCounter = dataSnapshot.getValue() == null ? 0 : (long) dataSnapshot.getValue();
                friendCounter = Math.max(friendCounter, 0);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }


    // listener to check if the the other is typing some message
    private ValueEventListener getTypingValueEventListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean typing = (dataSnapshot.getValue() != null);

                if (typing) {
                    mSubTitleTextView.setVisibility(View.VISIBLE);
                    mSubTitleTextView.setText("typing...");
                } else {
                    mSubTitleTextView.setVisibility(View.VISIBLE);

                    if (isFriendOnline)
                        mSubTitleTextView.setText("online");
                    else if (lastSeen != "")
                        mSubTitleTextView.setText(lastSeen);
                    else {
                        mSubTitleTextView.setVisibility(View.GONE);
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        };
    }


    // handle messages on server with recyclerView
    private ChildEventListener getChatRoomChildEventListener() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                MessageObject message = dataSnapshot.getValue(MessageObject.class);
                message.setMessageID(dataSnapshot.getKey());

                if (message.getState() == MessageObject.STATE_NOT_SENT && isIOnline) {
                    mChatRoomRef.child(message.getMessageID()).child("state").setValue(MessageObject.STATE_SENT);
                    updateMessageWithID(message);
                }


                if (mVisitedMessages.containsKey(message.getMessageID())) return;


                mVisitedMessages.put(message.getMessageID(), 1);

                mMessageList.add(message);
                mAdapter.notifyItemInserted(mMessageList.size() - 1);
                mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);


                if (message.getFrom() == null) return;
                //this is my friend message and not seen before
                if (message.getFrom().equals(mUser.getUid()) && message.getState() != MessageObject.STATE_SEEN) {
                    message.setState(MessageObject.STATE_SEEN);
                    mChatRoomRef.child(message.getMessageID()).child("state").setValue(MessageObject.STATE_SEEN);
                    mMyLastMessagesRef.child(Constants.FIREBASE_PROPERTY_COUNT).setValue(0);
                    mFriendLastMessagesRef.child(Constants.FIREBASE_PROPERTY_COUNT).setValue(-1);
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                MessageObject message = dataSnapshot.getValue(MessageObject.class);
                message.setMessageID(dataSnapshot.getKey());
                if (message.getState() == MessageObject.STATE_NOT_SENT && isIOnline) {
                    mChatRoomRef.child(message.getMessageID()).child("state").setValue(MessageObject.STATE_SENT);
                    message.setState(MessageObject.STATE_SEEN);
                }
                updateMessageWithID(message);
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


    @Override
    public void onStart() {
        super.onStart();
        mTypingRef.child(mUser.getUid()).addValueEventListener(mTypingValueEventListener);
        mChatRoomRef.orderByChild("date").addChildEventListener(mChatRoomChildEventListener);
        mFriendLastMessagesRef.child(Constants.FIREBASE_PROPERTY_COUNT).addValueEventListener(mCounterValueEventListener);
        traceFriendConnection();

    }

    @Override
    public void onStop() {
        super.onStop();
        mTypingRef.removeEventListener(mTypingValueEventListener);
        mChatRoomRef.removeEventListener(mChatRoomChildEventListener);
        mFriendLastMessagesRef.removeEventListener(mCounterValueEventListener);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        declareFirebase();

        mCloudinary = new Cloudinary(Constants.CLOUDINARY_URL);

        initActionBarWithUserData();

        // check if other is typing now
        mTypingValueEventListener = getTypingValueEventListener();


        mChatRoomChildEventListener = getChatRoomChildEventListener();


    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_chat_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new MessageAdapter(mMessageList);
        mRecyclerView.setAdapter(mAdapter);

        mFloatingActionButton = (FloatingActionButton) v.findViewById(R.id.message_box_float_button);
        mMessageBox = (EmojiEditText) v.findViewById(R.id.message_box_edit_text);
        mCameraView = (ImageView) v.findViewById(R.id.message_box_camera);
        mSmileImageView = (ImageView) v.findViewById(R.id.emoji_btn);


        emojiPopup = EmojiPopup.Builder.fromRootView(v).build(mMessageBox);
        mSmileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!emojiPopup.isShowing()) {
                    mSmileImageView.setImageResource(R.drawable.ic_keyboard);
                } else {
                    mSmileImageView.setImageResource(R.drawable.ic_smile);
                }
                emojiPopup.toggle(); // Toggles visibility of the Popup
            }
        });
        //    emojiPopup.dismiss(); // Dismisses the Popup
        //  emojiPopup.isShowing(); // Returns true when Popup is showing

        // prevent message box from overlaying messages
        mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    mRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                        }
                    }, 0);
                }
            }
        });


        //take photo
        mCameraView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    takePhoto();
                else callCameraApp();
            }
        });


        // check  if message box has value now
        mMessageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTypingRef.child(mMyUid).setValue(true);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mMessageBox.getText().toString().length() == 0) {
                    mFloatingActionButton.setImageResource(R.drawable.voice_recorder);
                    mMessageBoxState = 0;
                    mCameraView.setVisibility(View.VISIBLE);
                    mTypingRef.child(mMyUid).setValue(null);
                } else {
                    mFloatingActionButton.setImageResource(R.drawable.ic_send);
                    mMessageBoxState = 1;
                    mCameraView.setVisibility(View.GONE);
                    mTypingRef.child(mMyUid).setValue(true);
                }
            }
        });

        mFloatingActionButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendTextMessage();
                    }
                }

        );

        return v;
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSmileImageView.getWindowToken(), 0);
    }


    private void sendTextMessage() {
        if (mMessageBoxState == 1) {

            final MessageObject message = new MessageObject(mMessageBox.getText().toString().trim(),
                    ServerValue.TIMESTAMP,
                    mMyUid,
                    mUser.getUid());
            if (!isIOnline) message.setState(MessageObject.STATE_NOT_SENT);

            DatabaseReference order = mChatRoomRef.push();
            message.setMessageID(order.getKey());

            final String messageId = message.getMessageID();

            mVisitedMessages.put(messageId, 1);
            mMessageList.add(message);
            mAdapter.notifyItemInserted(mMessageList.size() - 1);


            order.setValue(message, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        getMessageFromChatRoom(messageId);
                    }
                }
            });

            updateLastMessageNode(messageId);


            mRecyclerView.scrollToPosition(mMessageList.size() - 1);
            mMessageBox.setText("");
        }
    }

    private void updateLastMessageNode(String messageId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(Constants.FIREBASE_PROPERTY_COUNT, 0);
        map.put(Constants.FIREBASE_PROPERTY_ID, messageId);
        mMyLastMessagesRef.setValue(map);

        HashMap<String, Object> fmap = new HashMap<>();
        fmap.put(Constants.FIREBASE_PROPERTY_COUNT, ++friendCounter);
        fmap.put(Constants.FIREBASE_PROPERTY_ID, messageId);
        mFriendLastMessagesRef.setValue(fmap);
    }

    private void takePhoto() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            callCameraApp();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(getActivity(), "External Storage READ Required to send photo", Toast.LENGTH_SHORT).show();
            }

            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE

            }, CAMERA_PERMISSION_REQUEST);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    ) {
                callCameraApp();
            } else {
                Toast.makeText(getActivity(), "App can't save photo without Media access Permission", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(CAMERA_PERMISSION_REQUEST, permissions, grantResults);
        }
    }


    private void callCameraApp() {
        try {
            mFile = ImageUtils.createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (mFile != null) {
            Uri uri = Uri.fromFile(mFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            getActivity().startActivityForResult(captureImage, CAMERA_REQUEST);
        }
    }


    public void sendPhotoView(String path) {
        new PhotoTask().execute(path);
    }


    private class PhotoTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... path) {

            String s = "";
            if (path.length == 0) return s;
            try {

                Map mp = mCloudinary.uploader().upload(
                        ImageUtils.CompressImage(ImageUtils.getPrefWidth(getContext()), getContext(), path[0])
                        , ObjectUtils.asMap()
                );

                s = mp.get("url").toString();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                s = "1";
            }

            return s;
        }

        @Override
        protected void onPostExecute(String s) {
            //     mAlertDialog.dismiss();
            if (s == null || s.length() == 0) {
                if (getActivity() != null)
                    Toast.makeText(getActivity(), "image not sent", Toast.LENGTH_SHORT).show();
                return;
            } else if (s.equals("1")) {
                Toast.makeText(getContext(), "not uploaded Please Fix mobile time", Toast.LENGTH_SHORT).show();
                return;
            }
            final MessageObject ms = new MessageObject(s, ServerValue.TIMESTAMP, MessageObject.IMAGE, mMyUid, mUser.getUid());
            //Firebase ord = mRef.push();
            DatabaseReference order = mChatRoomRef.push();
            ms.setMessageID(order.getKey());
            order.setValue(ms, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });
            mFile = null;
            mMessageList.add(ms);
            mVisitedMessages.put(ms.getMessageID(), 1);
            mAdapter.notifyDataSetChanged();
            mRecyclerView.scrollToPosition(mMessageList.size() - 1);
        }
    }


    private void updateMessageWithID(MessageObject m) {
        for (int i = mMessageList.size() - 1; i > -1; i--) {
            MessageObject message = mMessageList.get(i);
            if (message.getMessageID().equals(m.getMessageID())) {
                mMessageList.set(i, m);
                mAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    private void getMessageFromChatRoom(String messageID) {
        mChatRoomRef.child(messageID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MessageObject message = dataSnapshot.getValue(MessageObject.class);
                message.setMessageID(dataSnapshot.getKey());
                updateMessageWithID(message);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private class MessageAdapter extends RecyclerView.Adapter<MessageHolder> {
        List<MessageObject> mMessages;

        public MessageAdapter(List<MessageObject> list) {
            mMessages = list;
        }

        @Override
        public int getItemViewType(int position) {
            int val = 2 * mMessages.get(position).getMessageType();

            if (!mMyUid.equals(mMessages.get(position).getFrom()))
                val++;
            //Toast.makeText(getActivity(),"val="+val,Toast.LENGTH_SHORT).show();
            return val;
        }

        @Override
        public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            switch (viewType) {
                case 0:
                    View v = inflater.inflate(R.layout.out_going_text_holder, parent, false);
                    return new MessageHolder(v, 0);
                case 1:
                    View vv = inflater.inflate(R.layout.in_coming_text_holder, parent, false);
                    return new MessageHolder(vv, 1);
                case 2:
                    View v2 = inflater.inflate(R.layout.out_going_image_holder, parent, false);
                    return new MessageHolder(v2, 2);
                case 3:
                    View v3 = inflater.inflate(R.layout.in_coming_image_holder, parent, false);
                    return new MessageHolder(v3, 3);
                case 4:
                    View v4 = inflater.inflate(R.layout.out_going_video_holder, parent, false);
                    return new MessageHolder(v4, 4);
                case 5:
                    View v5 = inflater.inflate(R.layout.in_coming_video_holder, parent, false);
                    return new MessageHolder(v5, 5);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(MessageHolder holder, int position) {
            holder.bindView(mMessages.get(position));
        }

        @Override
        public int getItemCount() {
            return mMessages.size();
        }
    }


    private class MessageHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private TextView dateView;
        private TextView messageView;
        private ImageView mStatusImageView;
        private ImageView mImageView;
        private VideoView mVideoView;
        private int mID;
        private String link;


        public MessageHolder(View view, int type) {
            super(view);
            mID = type;
            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);
            if (mID < 2)
                messageView = (TextView) itemView.findViewById(R.id.holder_text);
            else if (mID < 4) {
                mImageView = (ImageView) itemView.findViewById(R.id.image_holder);
                //  mImageView.setOnClickListener(this);
            } else if (mID < 6)
                mVideoView = (VideoView) itemView.findViewById(R.id.video_holder);

            dateView = (TextView) itemView.findViewById(R.id.date);
            mStatusImageView = (ImageView) itemView.findViewById(R.id.status);
        }


        public void bindView(MessageObject mo) {

            if (mID < 2)
                messageView.setText(mo.getMessage());
            else if (mID < 4) {
                link = mo.getMessage();
                //  Toast.makeText(getActivity(),"hhh "+mo.getMessage(),Toast.LENGTH_SHORT).show();
                Picasso.with(getActivity())
                        .load(Uri.parse(link))
                        .resize(250, 250)
                        .centerCrop()
                        .transform(new CircleTransform(CircleTransform.ROUNDED_EDGES_BITMAP))
                        .into(mImageView);

            } else if (mID < 6) ;
            // mVideoView.

            String time = "";
            try {
                time = Utils.SIMPLE_TIME_FORMAT.format(mo.getDate());

            } catch (Exception e) {
                time = Utils.SIMPLE_TIME_FORMAT.format(new Date());
                mo.setState(MessageObject.STATE_NOT_SENT);
            }

            time = time.replace(".", "");

            dateView.setText(time);
            if (mID % 2 == 0) {
                if (mo.getState() == MessageObject.STATE_SEEN) {
                    mStatusImageView.setImageResource(R.drawable.ic_seen);
                } else if (mo.getState() == MessageObject.STATE_SENT) {
                    mStatusImageView.setImageResource(R.drawable.ic_action_tick);
                } else if (mo.getState() == MessageObject.STATE_DELIVERED) {
                    mStatusImageView.setImageResource(R.drawable.ic_done_all);
                } else {
                    mStatusImageView.setImageResource(R.drawable.ic_not_sent);
                }
            }

        }


        @Override
        public void onClick(View v) {
            if (mID == 2 || mID == 3) {
                Intent i = new Intent(getActivity(), ImageActivity.class);
                i.putExtra(ImageActivity.EXTRA_IMAGE_LINK, link);
                startActivity(i);
            }
        }


        @Override
        public boolean onLongClick(View v) {
            //   Toast.makeText(getContext(),"Long press hh",Toast.LENGTH_SHORT).show();
            final CharSequence[] items;
            if (mID < 2)
                items = new CharSequence[]{"Copy Text", "Delete"};
            else items = new CharSequence[]{"Save Image", "Delete"};

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Message");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    Toast.makeText(getContext(), items[item], Toast.LENGTH_SHORT).show();

                }
            });

            AlertDialog alert = builder.create();
            alert.show();
            return true;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_chat, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        } else if (id == R.id.action_send) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                Toast.makeText(getContext(), "Select multiple images limited to 10", Toast.LENGTH_LONG).show();
            }


            getActivity().startActivityForResult(Intent.createChooser(intent,
                    "Select Picture"), PICK_IMAGE_REQUEST);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onPause() {
        super.onPause();
        mTypingRef.child(mMyUid).removeValue();
    }


}
