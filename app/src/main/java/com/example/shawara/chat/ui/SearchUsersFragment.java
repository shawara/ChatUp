package com.example.shawara.chat.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import com.example.shawara.chat.ui.home.FriendsAdapter;
import com.example.shawara.chat.utils.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shawara on 9/17/2016.
 */

public class SearchUsersFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private FriendsAdapter mAdapter;
    private DatabaseReference mDatabase;
    private final static String TAG = "FriendsListFragment";
    private SearchView mSearchView;


    public static SearchUsersFragment newInstance() {
        return new SearchUsersFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friends_list, container, false);


        mDatabase = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.FIREBASE_URL_USERS);
        mDatabase.keepSynced(true);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.friends_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new FriendsAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        return v;
    }


    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        final MenuItem searchItem = menu.findItem(R.id.app_bar_search);


        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setQueryHint(getString(R.string.search_query_hint));
        mSearchView.setFocusable(true);
        mSearchView.setIconified(false);
        mSearchView.requestFocusFromTouch();


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

                updateUsers(querySearchForUser(lowerCaseQ));

                return false;
            }
        });


    }


    private void updateUsers(Query query) {

        query.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        // Toast.makeText(getActivity(), "count = " + dataSnapshot.getChildrenCount(), Toast.LENGTH_SHORT).show();
                        List<User> users = new ArrayList<>();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            User user = ds.getValue(User.class);
                            user.setUid(ds.getKey());
                            users.add(user);

                        }

                        mAdapter.setUsers(users);
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        // ...
                    }
                });
    }


    private Query querySearchForUser(String searchValue) {
        Query myFriendsQuery = mDatabase.orderByChild("nameCaseIgnore")
                .startAt(searchValue)
                .endAt(searchValue + (searchValue.length() > 0 ? "\uf8ff" : ""))
                .limitToFirst(50);
        return myFriendsQuery;
    }


    private Query queryMyFriends() {
        Query myFriendsQuery = mDatabase.orderByChild("nameCaseIgnore");
        return myFriendsQuery;
    }


}
