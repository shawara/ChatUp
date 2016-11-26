package com.example.shawara.chat.ui.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shawara.chat.R;
import com.example.shawara.chat.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shawara on 9/22/2016.
 */

public class FriendsAdapter extends RecyclerView.Adapter<UserHolder> {
    private Context mContext;
    private String mSearchStr = "";

    public FriendsAdapter(Context context) {
        mContext = context;
    }

    private List<User> mUsers = new ArrayList<>();

    public void setUsers(List<User> users) {
        mUsers = users;
    }

    public void setSearchStr(String searchStr) {
        mSearchStr = searchStr;
    }

    @Override
    public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(mContext).inflate(R.layout.friends_list_item, parent, false);
        return new UserHolder(v);
    }

    @Override
    public void onBindViewHolder(UserHolder holder, int position) {
        holder.bindView(mContext, mUsers.get(position), mSearchStr);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }


}

