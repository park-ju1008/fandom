package com.info.idol.community;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.info.idol.community.retrofit.ApiService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class JoinActivity extends AppCompatActivity {
    private TextView text_id,text_nickname,text_pw,text_pw2;
    private ApiService retrofitApiService;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        initView();
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl(ApiService.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofitApiService =retrofit.create(ApiService.class);
    }


    private void initView(){
        text_id=(TextView)findViewById(R.id.text_id);
        text_nickname=(TextView)findViewById(R.id.text_nickname);
        text_pw=(TextView)findViewById(R.id.text_pw);
        text_pw2=(TextView)findViewById(R.id.text_pw2);
        text_id.setOnFocusChangeListener(mFocusChangeListener);
        text_nickname.setOnFocusChangeListener(mFocusChangeListener);
        text_pw.setOnFocusChangeListener(mFocusChangeListener);
        text_pw2.setOnFocusChangeListener(mFocusChangeListener);

    }

    final Button.OnFocusChangeListener mFocusChangeListener= new Button.OnFocusChangeListener(){

        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            Log.d("af", "onFocusChange: "+hasFocus);
            if (!hasFocus) {
                switch (view.getId()) {
                    case R.id.text_id:
                        Log.d("Af", "onFocusChangeaaa: "+TextUtils.isEmpty(text_id.getText()));
                        if(TextUtils.isEmpty(text_id.getText())||!Patterns.EMAIL_ADDRESS.matcher(text_id.getText()).matches()){
                            Toast.makeText(view.getContext(),"올바른 이메일을 입력하세요",Toast.LENGTH_SHORT).show();
                        }else{

                        }
                        break;
                    case R.id.text_nickname:
                        break;
                    case R.id.text_pw:
                        break;
                    case R.id.text_pw2:
                        break;
                }
            }
        }
    };
}
