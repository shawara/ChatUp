package com.example.shawara.chat.ui.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shawara.chat.R;
import com.example.shawara.chat.model.User;
import com.example.shawara.chat.utils.Constants;
import com.example.shawara.chat.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shawara on 9/17/2016.
 */

public class FriendsListFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private FriendsAdapter mAdapter;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private final static String TAG = "FriendsListFragment";
    private SearchView mSearchView;
    private List<User> mFriendsList = new ArrayList<>();
    private LinearLayoutManager mLinearLayoutManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public void onStart() {
        super.onStart();
        getFriends();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friends_list, container, false);

        mUsersDatabase = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.FIREBASE_URL_USERS);

        String uid = Utils.getUid();
        mFriendsDatabase = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.FIREBASE_URL_FRIENDS + "/" + uid);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase.keepSynced(true);


        mRecyclerView = (RecyclerView) v.findViewById(R.id.friends_recycler_view);


        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new FriendsAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

//        getFriends();
        return v;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        mSearchView = (SearchView) searchItem.getActionView();

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "QueryTextSubmit: " + query);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Log.d(TAG, "QueryTextChange: " + newText);
                String lowerCaseQ = newText.toLowerCase();
                return false;
            }
        });


    }


    private void getFriends() {
        final List<String> uids = new ArrayList<>();

        mFriendsDatabase.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //  Toast.makeText(getContext(), "uids", Toast.LENGTH_SHORT).show();
                        for (DataSnapshot uid : dataSnapshot.getChildren()) {
                            uids.add(uid.getKey());
                        }
                        getUsers(uids);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }


    private void getUsers(final List<String> friendsUid) {
        mFriendsList.clear();

        for (int i = 0; i < friendsUid.size(); i++) {
            mUsersDatabase.child(friendsUid.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    user.setUid(dataSnapshot.getKey());
                    mFriendsList.add(user);
                    if (mFriendsList.size() == friendsUid.size()) {
                        mAdapter.setUsers(mFriendsList);
                        mAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        if (mFriendsList.size() == 0) {
            mAdapter.setUsers(mFriendsList);
            mAdapter.notifyDataSetChanged();
        }

    }


}
