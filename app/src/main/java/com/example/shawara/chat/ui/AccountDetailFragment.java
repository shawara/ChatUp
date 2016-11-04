package com.example.shawara.chat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shawara.chat.R;
import com.example.shawara.chat.model.User;
import com.example.shawara.chat.utils.Constants;
import com.example.shawara.chat.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;

/**
 * Created by shawara on 9/23/2016.
 */

public class AccountDetailFragment extends Fragment {
    private User mUser;
    private ImageView mBackDropImageView;
    private TextView mEmailTextView;
    private TextView mCreatedAtTextView;
    private FloatingActionButton mFriendFAB;
    private FloatingActionButton mNewMessageFAB;
    private boolean isFriend;

    private DatabaseReference mFriendsDatabase;

    public static AccountDetailFragment newInstance() {
        return new AccountDetailFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String myUid = Utils.getUid();
        mFriendsDatabase = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.FIREBASE_URL_FRIENDS + "/" + myUid);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_account_detail, container, false);
        mUser = (User) getActivity().getIntent().getSerializableExtra(AccountDetailActivity.EXTRA_USER);

        final Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //initialize views
        mBackDropImageView = (ImageView) v.findViewById(R.id.account_detail_backdrop);
        mFriendFAB = (FloatingActionButton) v.findViewById(R.id.account_detail_fab_add_friend);
        mNewMessageFAB = (FloatingActionButton) v.findViewById(R.id.account_detail_fab_write_new_message);
        mEmailTextView = (TextView) v.findViewById(R.id.account_detail_email);
        mCreatedAtTextView = (TextView) v.findViewById(R.id.account_detail_created_at);


        //set data to views
        mNewMessageFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = ChatActivity.newIntent(getContext(), mUser);
                startActivity(i);
            }
        });


        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) v.findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(mUser.getName());

        Picasso.with(getActivity())
                .load(mUser.getProfileImageUrl())
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .into(mBackDropImageView);

        mEmailTextView.setText(mUser.getEmail());

        long date = (long) mUser.getTimestampJoined().get(Constants.FIREBASE_PROPERTY_TIMESTAMP);
        mCreatedAtTextView.setText(Utils.SIMPLE_DATE_FORMAT.format(date));


        mFriendFAB.setEnabled(false);
        checkIsFriend();
        mFriendFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFriend) {
                    deleteFriend();
                    Toast.makeText(getContext(), mUser.getName() + " is deleted successfully from your friends.", Toast.LENGTH_SHORT).show();
                } else {
                    addFriend();
                    Toast.makeText(getContext(), mUser.getName() + " is added successfully to your friends.", Toast.LENGTH_SHORT).show();
                }
                isFriend = !isFriend;
                handleFriendButtonIcon();
            }
        });

        return v;
    }


    private void handleFriendButtonIcon() {
        if (isFriend) {
            mFriendFAB.setImageResource(R.drawable.unfriend);
        } else {
            mFriendFAB.setImageResource(R.drawable.ic_add_new_friend);
        }
    }


    private void checkIsFriend() {
        mFriendsDatabase.child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                isFriend = (dataSnapshot.getValue() != null);
                handleFriendButtonIcon();
                mFriendFAB.setEnabled(true);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void addFriend() {
        mFriendsDatabase.child(mUser.getUid()).setValue(true);
    }

    private void deleteFriend() {
        mFriendsDatabase.child(mUser.getUid()).setValue(null);
    }


}
