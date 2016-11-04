package com.example.shawara.chat.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.example.shawara.chat.model.User;

/**
 * Created by shawara on 9/23/2016.
 */

public class AccountDetailActivity extends SingleFragmentActivity {
    public static final String EXTRA_USER = "user_data";

    public static Intent newIntent(Context c, User user) {
        Intent i = new Intent(c, AccountDetailActivity.class);
        i.putExtra(EXTRA_USER, user);
        return i;
    }

    @Override
    protected Fragment createFragment() {
        return AccountDetailFragment.newInstance();
    }
}
