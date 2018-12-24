package com.info.idol.community;

import android.support.v7.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    protected String domain="http://35.229.103.161/uploads/";

    public void progressON() {
        GlobalApplication.getGlobalApplicationContext().progressON(this, null);
    }

    public void progressON(String message) {
        GlobalApplication.getGlobalApplicationContext().progressON(this, message);
    }

    public void progressOFF() {
        GlobalApplication.getGlobalApplicationContext().progressOFF();
    }


}

