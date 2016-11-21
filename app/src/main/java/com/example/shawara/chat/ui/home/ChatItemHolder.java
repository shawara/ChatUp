package com.example.shawara.chat.ui.home;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shawara.chat.R;
import com.example.shawara.chat.model.MessageObject;
import com.example.shawara.chat.model.User;
import com.example.shawara.chat.ui.AccountDetailActivity;
import com.example.shawara.chat.ui.ChatActivity;
import com.example.shawara.chat.utils.Utils;
import com.facebook.drawee.view.SimpleDraweeView;


/**
 * Created by shawara on 10/2/2016.
 */

public class ChatItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    // private ImageView mProfileImage;
    private SimpleDraweeView mProfileImage;
    private ImageView mMessageStatus;
    private TextView mNameTextView;
    private TextView mMessageTextView;
    private TextView mCounterTextView;
    private TextView mDateTextView;

    private User mUser;
    private MessageObject mMessage;
    private Long mCount;
    private Context mContext;


    public ChatItemHolder(View itemView, Context context) {
        super(itemView);
        mContext = context;

        mProfileImage = (SimpleDraweeView) itemView.findViewById(R.id.chat_list_item_friend_image_view);
        //mProfileImage = (ImageView) itemView.findViewById(R.id.chat_list_item_friend_image_view);
        mMessageStatus = (ImageView) itemView.findViewById(R.id.chat_list_item_message_status);
        mNameTextView = (TextView) itemView.findViewById(R.id.chat_list_item_friend_name);
        mMessageTextView = (TextView) itemView.findViewById(R.id.chat_list_item_message_text_view);
        mCounterTextView = (TextView) itemView.findViewById(R.id.chat_list_item_message_count);
        mDateTextView = (TextView) itemView.findViewById(R.id.chat_list_item_date);

        mProfileImage.setOnClickListener(this);
        mMessageStatus.setOnClickListener(this);
        mNameTextView.setOnClickListener(this);
        mMessageTextView.setOnClickListener(this);
        mCounterTextView.setOnClickListener(this);
        mDateTextView.setOnClickListener(this);

    }

    public void bindData(ChatListFragment.ChatItem chatItem) {
        mUser = chatItem.user;
        mMessage = chatItem.message;
        mCount = chatItem.count;

        setProfileImage();
        mNameTextView.setText(mUser.getName());
        setMessageText();
        setMessageStatus();
        setCountTextView(mCount);
        mDateTextView.setText(Utils.getRelativeTime((long) mMessage.getDate()));


    }

    private void setProfileImage() {
//        Picasso.with(mContext).load(mUser.getProfileImageUrl())
//                .placeholder(R.drawable.default_profile)
//                .error(R.drawable.default_profile)
//                .into(mProfileImage);
        mProfileImage.setImageURI(mUser.getProfileImageUrl());
    }

    private void setMessageText() {
        if (mCount > 0)
            mMessageTextView.setTextColor(Color.parseColor("#000000"));
        else
            mMessageTextView.setTextColor(Color.parseColor("#474747"));

        if (mMessage.getMessageType() == MessageObject.TEXT) {
            mMessageTextView.setText(mMessage.getMessage());
        } else if (mMessage.getMessageType() == MessageObject.IMAGE) {
            mMessageTextView.setText(R.string.image);
        }
    }

    private void setMessageStatus() {
      //  Log.d("hhh", "setMessageStatus: "+mUser.getUid()+" "+mMessage.getMessageID());
        if (mMessage.getFrom().equals(mUser.getUid()))
            mMessageStatus.setVisibility(View.GONE);
        else {
            mMessageStatus.setVisibility(View.VISIBLE);
            if (mMessage.getState() == MessageObject.STATE_SEEN) {
                mMessageStatus.setImageResource(R.drawable.ic_seen);
            } else if (mMessage.getState() == MessageObject.STATE_SENT) {
                mMessageStatus.setImageResource(R.drawable.ic_action_tick);
            } else if (mMessage.getState() == MessageObject.STATE_DELIVERED) {
                mMessageStatus.setImageResource(R.drawable.ic_done_all);
            } else {
                mMessageStatus.setImageResource(R.drawable.ic_not_sent);
            }
        }
    }

    private void setCountTextView(Long count) {
        if (count > 0) {
            mCounterTextView.setVisibility(View.VISIBLE);
            mCounterTextView.setText(count + "");
        } else {
            mCounterTextView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.chat_list_item_friend_image_view) {
            mContext.startActivity(AccountDetailActivity.newIntent(mContext, mUser));
        } else {
            mContext.startActivity(ChatActivity.newIntent(mContext, mUser));
        }
    }
}