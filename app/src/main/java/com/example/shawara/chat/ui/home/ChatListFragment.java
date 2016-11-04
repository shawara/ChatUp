package com.example.shawara.chat.ui.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shawara.chat.R;
import com.example.shawara.chat.model.MessageObject;
import com.example.shawara.chat.model.User;
import com.example.shawara.chat.utils.Constants;
import com.example.shawara.chat.utils.Utils;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shawara on 9/29/2016.
 */

public class ChatListFragment extends Fragment {
    private final static String TAG = "ChatListFragment";

    private RecyclerView mRecyclerView;
    private ChatListAdapter mChatListAdapter;

    private DatabaseReference mLastMessageRef;
    private DatabaseReference mUsersRef;
    private DatabaseReference mChatRef;
    private ChildEventListener mLastMessageChildEventListener;
    // private DatabaseReference mFriendLastMessagesRef;

    private String mMyUid;

    private List<User> mFriendsList = new ArrayList<>();
    private List<MessageObject> mLastMessages = new ArrayList<>();
    private List<Long> mCountList = new ArrayList<>();


    public static class ChatItem {
        public MessageObject message = new MessageObject();
        public User user = new User();
        public long count = 0;

        public ChatItem(String userid, String messageid, long count) {
            message.setMessageID(messageid);
            user.setUid(userid);
            this.count = count;
        }
    }

    private List<ChatItem> mChatItemList = new ArrayList<>();

    private ChildEventListener getLastMessageChildEventListener() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String userId = dataSnapshot.getKey();
                long count = (long) dataSnapshot.child(Constants.FIREBASE_PROPERTY_COUNT).getValue();
                String messageId = dataSnapshot.child(Constants.FIREBASE_PROPERTY_ID).getValue().toString();


                ChatItem chatItem = new ChatItem(userId, messageId, count);

                if (count > 0) {
                    markAsDelevered(chatItem);
                }

                if (count < -1000) {
                    count += 1000;
                    count = -count;
                }
                chatItem.count = count;
                getUser(chatItem);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String userId = dataSnapshot.getKey();
                String messageId = dataSnapshot.child(Constants.FIREBASE_PROPERTY_ID).getValue().toString();
                long count = (long) dataSnapshot.child(Constants.FIREBASE_PROPERTY_COUNT).getValue();
                int idx = getChatItemIndex(userId);
                ChatItem chatItem;
                if (idx != -1)
                    chatItem = mChatItemList.get(idx);
                else chatItem = new ChatItem(userId, messageId, count);

                chatItem.message.setMessageID(messageId);

                if (count > 0) {
                    markAsDelevered(chatItem);
                }

                if (count < -1000) {
                    count += 1000;
                    count = -count;
                }
                chatItem.count = count;
                getMessage(chatItem, true, idx);

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
        }

                ;
    }

    private void markAsDelevered(ChatItem chatItem) {
        mChatRef.child(Utils.getRoomName(Utils.getUid(), chatItem.user.getUid()) + "/"
                + Constants.FIREBASE_LOCATION_MESSAGES + "/"
                + chatItem.message.getMessageID()).child("state").setValue(MessageObject.STATE_DELIVERED);

        //    mFriendLastMessagesRef
        //           .child(chatItem.user.getUid())
        //          .child(Utils.getUid())
        //        .child(Constants.FIREBASE_PROPERTY_COUNT).setValue(-9);
    }


    private int getChatItemIndex(String uid) {
        for (int i = 0; i < mChatItemList.size(); i++) {
            String id = mChatItemList.get(i).user.getUid();
            if (id.equals(uid)) {
                return i;
            }
        }
        return -1;
    }


    private void getUser(final ChatItem chatItem) {
        mUsersRef.child(chatItem.user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists())
                    return;
                User user = dataSnapshot.getValue(User.class);
                user.setUid(dataSnapshot.getKey());
                chatItem.user = user;
                getMessage(chatItem, false, -1);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getMessage(final ChatItem chatItem, final boolean chatItemExistInList, final int index) {
        final String chatRoomId = Utils.getRoomName(mMyUid, chatItem.user.getUid());
        // Log.d(TAG, "getMessage: myuid=" + mMyUid + " frienduid=" + chatItem.user.getUid() + " id=" + chatItem.message.getMessageID());

        mChatRef.child(chatRoomId + "/" + Constants.FIREBASE_LOCATION_MESSAGES + "/" + chatItem.message.getMessageID())
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String messagePath = chatRoomId + "/" + Constants.FIREBASE_LOCATION_MESSAGES + "/" + dataSnapshot.getKey();
                                //when message not synced yet.
                                if (!dataSnapshot.exists()) {
                                    return;
                                }
                                MessageObject message = dataSnapshot.getValue(MessageObject.class);
                                message.setMessageID(dataSnapshot.getKey());
                                chatItem.message = message;

                                if (chatItemExistInList) {
                                    int newIndx = orderChatItemInList(index);
                                    mChatListAdapter.notifyItemMoved(index, newIndx);
                                    mChatListAdapter.notifyItemChanged(newIndx);

                                } else {
                                    insertChatItemInList(chatItem);
                                }

                                mChatRef.child(messagePath).removeEventListener(this);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }

                );
    }


    private int insertChatItemInList(ChatItem chatItem) {
        mChatItemList.add(chatItem);
        int idx = mChatItemList.size() - 1;
        int index = orderChatItemInList(idx);
        mChatListAdapter.notifyItemInserted(index);
        return index;
    }

    private int orderChatItemInList(int idx) {
        long curDate = (long) mChatItemList.get(idx).message.getDate();
        while (idx > 0) {
            long prevDate = (long) mChatItemList.get(idx - 1).message.getDate();
            if (curDate > prevDate) {
                swap(idx, idx - 1);
            } else {
                break;
            }
            idx--;
        }
        return idx;
    }

    private void swap(int i, int j) {
        ChatItem temp = mChatItemList.get(i);
        mChatItemList.set(i, mChatItemList.get(j));
        mChatItemList.set(j, temp);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat_list, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_chat_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mChatListAdapter = new ChatListAdapter();
        mRecyclerView.setAdapter(mChatListAdapter);
        mChatListAdapter.setChatItemList(mChatItemList);

        return v;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMyUid = Utils.getUid();
        //      mFriendLastMessagesRef = FirebaseDatabase.getInstance()
//                .getReferenceFromUrl(Constants.FIREBASE_URL_LAST_MESSAGE);

        mChatRef = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.FIREBASE_URL_MESSAGES);
        mChatRef.keepSynced(true);

        mLastMessageRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(Constants.FIREBASE_URL_LAST_MESSAGE).child(mMyUid);
        mLastMessageRef.keepSynced(true);

        mUsersRef = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.FIREBASE_URL_USERS);


        mLastMessageChildEventListener = getLastMessageChildEventListener();
        mLastMessageRef.addChildEventListener(mLastMessageChildEventListener);
        // getLastMessagesCountList();
    }


    private class ChatListAdapter extends RecyclerView.Adapter<ChatItemHolder> {
        private List<ChatItem> mChatItemList = new ArrayList<>();

        public void setChatItemList(List<ChatItem> chatItemList) {
            mChatItemList = chatItemList;
        }

        @Override
        public ChatItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.chat_list_item, parent, false);
            return new ChatItemHolder(v, getContext());
        }

        @Override
        public void onBindViewHolder(ChatItemHolder holder, int index) {
            holder.bindData(mChatItemList.get(index));
        }

        @Override
        public int getItemCount() {
            return mChatItemList.size();
        }
    }
}
