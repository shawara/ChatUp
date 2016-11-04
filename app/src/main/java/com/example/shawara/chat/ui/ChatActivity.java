package com.example.shawara.chat.ui;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

import com.example.shawara.chat.R;
import com.example.shawara.chat.model.User;
import com.example.shawara.chat.utils.ImageUtils;
import com.vanniktech.emoji.EmojiPopup;

import java.util.ArrayList;

public class ChatActivity extends SingleFragmentActivity {
    public static final String EXTRA_USER = "user_data";

    public static Intent newIntent(Context c, User user) {
        Intent i = new Intent(c, ChatActivity.class);
        i.putExtra(EXTRA_USER, user);
        return i;
    }

    @Override
    protected Fragment createFragment() {
        return ChatFragment.newInstance();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Toast.makeText(getBaseContext(),"rc="+(requestCode ),Toast.LENGTH_SHORT).show();

        if (resultCode == RESULT_OK) {
            ChatFragment fragment = (ChatFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);

            if (requestCode == ChatFragment.CAMERA_REQUEST) {
                fragment.sendPhotoView(ImageUtils.mFile.getAbsolutePath());

            } else if (requestCode == ChatFragment.PICK_IMAGE_REQUEST) {
                if (data.getData() == null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

                        ClipData clipData = data.getClipData();
                        if (clipData != null) {
                            for (int i = 0; i < Math.min(clipData.getItemCount(), 10); i++) {
                                ClipData.Item item = clipData.getItemAt(i);
                                Uri uri = item.getUri();
                                String path = ImageUtils.getPath(getContentResolver(), uri);
                                fragment.sendPhotoView(path);
                            }
                        }
                    }
                } else {

                    Uri selectedImageUri = data.getData();
                    String path = ImageUtils.getPath(getContentResolver(), selectedImageUri);
                    fragment.sendPhotoView(path);
                }

            }
        }
    }


    @Override
    public void onBackPressed() {
        ChatFragment fragment = (ChatFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment.emojiPopup.isShowing()) {
            fragment.emojiPopup.dismiss();
            fragment.mSmileImageView.setImageResource(R.drawable.ic_smile);
        } else {
            super.onBackPressed();
        }
    }
}
