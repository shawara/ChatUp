package com.example.shawara.chat.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;

import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shawara.chat.R;
import com.example.shawara.chat.model.User;
import com.example.shawara.chat.notification.MessageService;
import com.example.shawara.chat.ui.MyProfileActivity;
import com.example.shawara.chat.ui.SearchUsersActivity;
import com.example.shawara.chat.ui.login.LoginActivity;
import com.example.shawara.chat.ui.settings.SettingsActivity;
import com.example.shawara.chat.utils.Constants;
import com.example.shawara.chat.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


/**
 * Created by shawara on 9/14/2016.
 */

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private DrawerLayout mDrawerLayout;
    //   public static final String UID = "uid";
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar actionBar;
    private DatabaseReference mDatabase;
    private User mUser = null;
    private TextView mUsernameTextView;
    private ImageView mUserImageView;
    private FloatingActionButton mAddFriend;

    private int imageResId[] = {
            R.drawable.ic_gps,
            R.drawable.ic_chat,
            R.drawable.ic_friends_list
    };

    public static Intent newIntent(Context c) {
        Intent i = new Intent(c, HomeActivity.class);
        //   i.putExtra(UID, uid);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        return i;
    }


    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        moveTaskToBack(true);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(getBaseContext(), MessageService.class));

        setContentView(R.layout.activity_home);

        mDatabase = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.FIREBASE_URL_USERS);
        mDatabase.keepSynced(true);

        // Initilization
        viewPager = (ViewPager) findViewById(R.id.pager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager(), this);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
        viewPager.setAdapter(mAdapter);


        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setIcon(imageResId[i]);
        }


        // nav header data
        mUsernameTextView = (TextView) headerView.findViewById(R.id.nav_head_username);
        mUserImageView = (ImageView) headerView.findViewById(R.id.nav_head_user_image);

        mUserImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUser != null)
                    startActivity(MyProfileActivity.newIntent(getBaseContext(), mUser));
            }
        });
        mUsernameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUser != null)
                    startActivity(MyProfileActivity.newIntent(getBaseContext(), mUser));
            }
        });


        mAddFriend = (FloatingActionButton) findViewById(R.id.fab_add_friend);

        mAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), SearchUsersActivity.class));
            }
        });

        viewPager.setCurrentItem(1);
        getUserData();


        userPresence();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void getUserData() {
        String uid = Utils.getUid();
        mDatabase.child(uid).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) return;

                        mUser = dataSnapshot.getValue(User.class);
                        mUser.setUid(dataSnapshot.getKey());
                        updateUserData();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        // ...
                    }
                }

        );
    }


    private void updateUserData() {
        mUsernameTextView.setText(mUser.getName());

        Picasso.with(getBaseContext())
                .load(mUser.getProfileImageUrl())
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .into(mUserImageView);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_logout:
                logout();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        stopService(new Intent(HomeActivity.this, MessageService.class));
        startActivity(LoginActivity.newIntent(this));
        finish();
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        int id = menuItem.getItemId();
                        if (id == R.id.nav_logout) {
                            logout();
                        }

                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
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
