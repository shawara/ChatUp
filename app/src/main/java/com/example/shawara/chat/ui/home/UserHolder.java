package com.example.shawara.chat.ui.home;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shawara.chat.R;
import com.example.shawara.chat.model.User;
import com.example.shawara.chat.ui.AccountDetailActivity;
import com.example.shawara.chat.ui.ChatActivity;
import com.example.shawara.chat.utils.Constants;
import com.example.shawara.chat.utils.Utils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

/**
 * Created by shawara on 9/22/2016.
 */

public class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private ImageView mProfileAvatar;
    private TextView mTextView;
    private Context mContext;
    private User mUser;

    public UserHolder(View itemView) {
        super(itemView);
        mProfileAvatar = (ImageView) itemView.findViewById(R.id.friend_item_avatar);
        mTextView = (TextView) itemView.findViewById(R.id.friend_item_name);

        mProfileAvatar.setOnClickListener(this);
        mTextView.setOnClickListener(this);
    }

    public void bindView(Context c, User user) {
        mContext = c;
        mUser = user;

        Picasso.with(c).load(user.getProfileImageUrl())
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .into(mProfileAvatar);

        mTextView.setText(user.getName());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.friend_item_avatar) {
            mContext.startActivity(AccountDetailActivity.newIntent(mContext, mUser));
        } else if (id == R.id.friend_item_name) {
            final DatabaseReference membersRef =
                    FirebaseDatabase
                            .getInstance()
                            .getReferenceFromUrl(Constants.FIREBASE_URL_MESSAGES)
                            .child(Utils.getRoomName(Utils.getUid(), mUser.getUid()))
                            .child(Constants.FIREBASE_LOCATION_MEMBERS);
            HashMap<String, Boolean> map = new HashMap<>();
            map.put(Utils.getUid(), true);
            map.put(mUser.getUid(), true);

            membersRef.setValue(map);
            mContext.startActivity(ChatActivity.newIntent(mContext, mUser));
        }
    }
}