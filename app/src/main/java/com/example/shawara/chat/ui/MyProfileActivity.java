package com.example.shawara.chat.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;

import com.example.shawara.chat.R;
import com.example.shawara.chat.model.User;

/**
 * Created by shawara on 9/24/2016.
 */

public class MyProfileActivity extends SingleFragmentActivity {
    public static final String EXTRA_USER = "user_data";

    public static Intent newIntent(Context c, User user) {
        Intent i = new Intent(c, MyProfileActivity.class);
        i.putExtra(EXTRA_USER, user);
        return i;
    }

    @Override
    protected Fragment createFragment() {
        return MyProfileFragment.newInstance();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == MyProfileFragment.PICK_IMAGE_REQUEST) {
                Uri selectedImageUri = data.getData();
                MyProfileFragment fragment = (MyProfileFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                fragment.uploadProfileImage(selectedImageUri);

            }
        }
    }


}
