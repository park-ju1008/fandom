package com.info.idol.community;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;


public class JoinActivity extends AppCompatActivity {
    private TextView text_id,text_nickname,text_pw,text_pw2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
    }


    private void initView(){
        text_id=(TextView)findViewById(R.id.text_id);
        text_nickname=(TextView)findViewById(R.id.text_nickname);
        text_pw=(TextView)findViewById(R.id.text_pw);
        text_pw2=(TextView)findViewById(R.id.text_pw2);
    }
}
