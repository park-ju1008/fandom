package com.info.idol.community.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.info.idol.community.R;

public class ScheduleActivity extends BottomNavigationParentActivity {
    @Override
    public int getCurrentActivityLayoutName() {
        return R.layout.activity_schedule;
    }

    @Override
    public int getCurrentSelectedBottomMenuItemID() {
        return R.id.action_three;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView(){

    }
}
