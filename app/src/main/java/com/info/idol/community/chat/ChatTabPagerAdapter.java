package com.info.idol.community.chat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ChatTabPagerAdapter extends FragmentStatePagerAdapter {

    private int tabCount;

    public ChatTabPagerAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                TotalChatFragment totalChatFragment = new TotalChatFragment();
                return totalChatFragment;
            case 1:
                MyChatFragment myChatFragment = new MyChatFragment();
                return myChatFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}
