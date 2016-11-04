package com.example.shawara.chat.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;

import com.example.shawara.chat.R;

/**
 * Created by shawara on 9/22/2016.
 */

public class SearchUsersActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return SearchUsersFragment.newInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
