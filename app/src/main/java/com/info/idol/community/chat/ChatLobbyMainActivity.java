package com.info.idol.community.chat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.info.idol.community.BaseActivity;
import com.info.idol.community.R;

public class ChatLobbyMainActivity extends BaseActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby_main);
        initView();
    }

    void initView(){
        final Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar_mainLobby);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("톡톡");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tabLayout=(TabLayout)findViewById(R.id.tabLayout_mainLobby);

        ChatTabPagerAdapter pagerAdapter=new ChatTabPagerAdapter(getSupportFragmentManager(),tabLayout.getTabCount());
        viewPager=(ViewPager)findViewById(R.id.viewpager_mainLobby);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

}
