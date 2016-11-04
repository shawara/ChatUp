package com.example.shawara.chat.ui.home;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by shawara on 9/14/2016.
 */

public class TabsPagerAdapter extends FragmentPagerAdapter {

    private Context context;

    public TabsPagerAdapter(FragmentManager fm, Context c) {
        super(fm);
        context = c;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new LocationsListFragment();
            case 1:
                return new ChatListFragment();
            case 2:
                return new FriendsListFragment();

        }

        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }


}
