package com.info.idol.community;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

public class AddInfoActivity extends AppCompatActivity {

    private TextView text_token;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addinfo);
        SharedPreferences pref=getSharedPreferences("user",MODE_PRIVATE);
        String s=pref.getString("AccessToken","");
        text_token=(TextView)findViewById(R.id.text_accessToken);
        text_token.setText(s);
    }
}
